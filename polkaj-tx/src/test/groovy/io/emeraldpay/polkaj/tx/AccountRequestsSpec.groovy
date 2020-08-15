package io.emeraldpay.polkaj.tx


import io.emeraldpay.polkaj.types.Address
import io.emeraldpay.polkaj.types.ByteData
import io.emeraldpay.polkaj.types.DotAmount
import io.emeraldpay.polkaj.types.Hash256
import io.emeraldpay.polkaj.types.Hash512
import org.apache.commons.codec.binary.Hex
import spock.lang.Specification

class AccountRequestsSpec extends Specification {

    def "Prepare issuance request"() {
        when:
        def req = AccountRequests.totalIssuance()
        def act = Hex.encodeHexString(req.encodeRequest().bytes)
        then:
        act == "c2261276cc9d1f8598ea4b6a74b15c2f57c875e4cff74148e4628f264b974c80"
    }

    def "Decode issuance response"() {
        when:
        def req = AccountRequests.totalIssuance()
        def act = req.apply(ByteData.from("0xf70af5f6f3c843050000000000000000"))
        then:
        act == DotAmount.fromPlancks("379367743775116023")
    }

    def "Prepare balance request"() {
        when:
        def req = AccountRequests.balanceOf(Address.from("1WG3jyNqniQMRZGQUc7QD2kVLT8hkRPGMSqAb5XYQM1UDxN"))
        def act = Hex.encodeHexString(req.encodeRequest().bytes)
        then:
        act == "26aa394eea5630e07c48ae0c9558cef7b99d880ec681799c0cf30e8886371da9762b7694480fb50358c23ab18950158b1650c532ed1a8641e8922aa24ade0ff411d03edd9ed1c6b7fe42f1a801cee37c"
    }

    def "Decode balance response"() {
        setup:
        def req = AccountRequests.balanceOf(Address.from("1WG3jyNqniQMRZGQUc7QD2kVLT8hkRPGMSqAb5XYQM1UDxN"))
        def result = ByteData.from("0x1100000003f70af5f6f3c843050000000000000000000000000000000000000000000000000000c52ebca2b10000000000000000000000c52ebca2b1000000000000000000")
        when:
        def act = req.apply(result)
        then:
        act.nonce == 17
        act.refcount == 3
        with(act.data) {
            free == DotAmount.fromPlancks(379367743775116023)
            reserved == DotAmount.ZERO
            feeFrozen == DotAmount.fromDots(50000)
            miscFrozen == DotAmount.fromDots(50000)
        }
    }

    def "Encode transfer"() {
        when:
        def transfer = AccountRequests.transfer()
            .module(5, 0)
            .from(Address.from("5GrwvaEF5zXb26Fz9rcQpDWS57CtERHpNehXCPcNoHGKutQY"))
            .to(Address.from("5FHneW46xGXgs5mUiveU4sbTyGBzmstUspZC92UhjJM694ty"))
            .nonce(1234567890)
            .amount(DotAmount.fromDots(1.23))
            .signed(Hash512.from("0x6a141ade40871c076f3eb32362f0204db49e4ae37e5dc7a68329f1a6768034556201432b1635637fc1d42ae6fce996fb25ef175ee1ae4015d2b8769436d89987"))
            .build()
        def act = transfer.encodeRequest()
        then:
        act.toString() == "0x490284d43593c715fdd31c61141abd04a99fd6822c8558854ccde39a5684e7a56da27d016a141ade40871c076f3eb32362f0204db49e4ae37e5dc7a68329f1a6768034556201432b1635637fc1d42ae6fce996fb25ef175ee1ae4015d2b8769436d899870003d20296490005008eaf04151687736326c9fea17e25fc5287613693c912909cb226aa4794f26a480b008cb6611e01"
    }

    def "Sign and encode transfer"() {
        when:
        ExtrinsicContext context = ExtrinsicContext.newBuilder()
                .runtime(3, 0x12)
                .genesis(Hash256.from("0x4c0bdd177c17ca145ad9a3e76d092d4d4baa8add4fa8c78cc2fbbf8e3cbd5122"))
                .nonce(1234567890)
                .build()
        def transfer = AccountRequests.transfer()
                .module(5, 0)
                .from(Address.from("5GrwvaEF5zXb26Fz9rcQpDWS57CtERHpNehXCPcNoHGKutQY"))
                .to(Address.from("5FHneW46xGXgs5mUiveU4sbTyGBzmstUspZC92UhjJM694ty"))
                .amount(DotAmount.fromDots(1.23))
                .sign(TestKeys.aliceKey, context)
                .build()
        def act = transfer.encodeRequest()
        then:
        act.bytes.length == 148
    }
}
