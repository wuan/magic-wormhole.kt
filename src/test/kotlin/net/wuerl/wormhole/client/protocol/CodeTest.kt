package net.wuerl.wormhole.client.protocol

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class CodeFactoryTest {
    @MockK
    lateinit var randomWordFactory: RandomWordFactory

    @InjectMockKs
    lateinit var uut : CodeFactory

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks

    @Test
    fun returnsCode() {
        every { randomWordFactory.getOdd()} returns "bar"
        every { randomWordFactory.getEven() } returns "baz"

        val code = uut.create("foo")

        assertThat(code.nameplate).isEqualTo("foo")
        assertThat(code.password).isEqualTo("bar-baz")
    }
}

class RandomWordFactoryTest {
    @MockK
    lateinit var wordlist: Wordlist

    @MockK
    lateinit var random: SecureRandom

    @InjectMockKs
    lateinit var uut : RandomWordFactory

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true) // turn relaxUnitFun on for all mocks

    @Test
    fun generatesEvenWord() {
        val randomValue = 10
        every { random.nextInt(256)} returns randomValue
        every { wordlist.getEven(randomValue) } returns "evenWord"

        val result = uut.getEven()

        assertThat(result).isEqualTo("evenWord")
    }

    @Test
    fun generatesOddWord() {
        val randomValue = 15
        every { random.nextInt(256)} returns randomValue
        every { wordlist.getOdd(randomValue) } returns "oddWord"

        val result = uut.getOdd()

        assertThat(result).isEqualTo("oddWord")
    }
}
class WordlistTest {
    val uut = Wordlist()

    @Test
    fun shouldDeliverFirstValues() {
        assertThat(uut.getEven(0)).isEqualTo("aardvark")
        assertThat(uut.getOdd(0)).isEqualTo("adroitness")
    }

    @Test
    fun shouldDeliverLastValues() {
        assertThat(uut.getEven(255)).isEqualTo("zulu")
        assertThat(uut.getOdd(255)).isEqualTo("yucatan")
    }

    @Test
    fun shouldHaveAllValues() {
        IntRange(0, 255).forEach {
            uut.getOdd(it)
            uut.getEven(it)
        }
    }

    @Test
    fun shouldNotHaveOutsideValues() {
        assertThat(uut.getOdd(-1)).isNull()
        assertThat(uut.getEven(-1)).isNull()
        assertThat(uut.getOdd(256)).isNull()
        assertThat(uut.getEven(256)).isNull()
    }

    @Test
    fun shouldRaiseWhenWordsDoNotExist() {
        assertThatThrownBy { Wordlist("non-existent") }.isInstanceOf(IllegalStateException::class.java)
    }
}