package com.github.jwcranford.pphrasegen

import spock.lang.Specification

class EntropyCalculatorSpec extends Specification {

  def testPowerOfTwo() {
    given:
    final EntropyCalculator calc = new EntropyCalculator(1024)

    expect:
    1024 == calc.getEffectiveWordListSize()
    10 == calc.getEntropyPerWord()
  }

  def testNotPowerOfTwo() {
    given:
    final EntropyCalculator calc = new EntropyCalculator(1100)

    expect:
    1024 == calc.getEffectiveWordListSize()
    10 == calc.getEntropyPerWord()
  }
}
