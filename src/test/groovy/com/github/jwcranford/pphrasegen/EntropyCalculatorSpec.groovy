package com.github.jwcranford.pphrasegen

import spock.lang.Specification

class EntropyCalculatorSpec extends Specification {

  def testPowerOfTwo() {
    given:
    final EntropyCalculator calc = new EntropyCalculator(1024)

    expect:
    1024 == calc.getEffectiveWordListSize()
    8 == (int) Math.ceil(calc.calculateWordCount(75))
  }

  def testNotPowerOfTwo() {
    given:
    final EntropyCalculator calc = new EntropyCalculator(1100)

    expect:
    1024 == calc.getEffectiveWordListSize()
    8 == (int) Math.ceil(calc.calculateWordCount(75))
  }
}
