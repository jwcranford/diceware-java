package com.github.jwcranford.pphrasegen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EntropyCalculatorTest {

  @Test
  void testPowerOfTwo() {
    final EntropyCalculator calc = new EntropyCalculator(1024);
    assertEquals(1024, calc.getEffectiveWordListSize());
    assertEquals(8, (int) Math.ceil(calc.calculateWordCount(75)));
  }

  @Test
  void testNotPowerOfTwo() {
    final EntropyCalculator calc = new EntropyCalculator(1100);
    assertEquals(1024, calc.getEffectiveWordListSize());
    assertEquals(8, (int) Math.ceil(calc.calculateWordCount(75)));
  }
}
