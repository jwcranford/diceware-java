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
}
