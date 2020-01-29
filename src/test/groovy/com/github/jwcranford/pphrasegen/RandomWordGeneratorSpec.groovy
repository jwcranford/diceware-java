package com.github.jwcranford.pphrasegen

import spock.lang.Specification

class RandomWordGeneratorSpec extends Specification {

  def 'picks word from list using given random generator'() {
    given:
    def rand = Mock(Random)
    rand.nextInt(_) >> 2
    def r = new RandomWordGenerator(rand, ["adam", "bobby", "charlie"])

    expect:
    "charlie" == r.get()
  }

  def 'replace works as expected'() {
    expect:
    "!234" == RandomWordGenerator.replace(0, "1234", "!")
    "1!34" == RandomWordGenerator.replace(1, "1234", "!")
    "12!4" == RandomWordGenerator.replace(2, "1234", "!")
    "123!" == RandomWordGenerator.replace(3, "1234", "!")
  }

  def 'replaces char in random location'() {
    given:
    def rand = Mock(Random)
    rand.nextInt(_) >>> [2, // which index to replace
                         3] // which string to replace it with
    def r = new RandomWordGenerator(rand, ["0", "1", "2", "3"])

    expect:
    "al3ha" == r.replaceRandomChar("alpha")
  }

  def 'replaces chars repeatedly in random locations'() {
    given:
    def rand = Mock(Random)
    // in each pair: (which index to replace), (what to replace it with)
    rand.nextInt(_) >>> [0, 0,
                         2, 2,
                         4, 3]
    def r = new RandomWordGenerator(rand, ["0", "1", "2", "3"])

    expect:
    "0l2h3" == r.replaceRepeatedly(3).apply("alpha")
  }
}
