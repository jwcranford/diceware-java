package com.github.jwcranford.pphrasegen

import spock.lang.Specification

class PassphraseGenCliSpec extends Specification {

  def addRandomCharRepeatedly() {
    given:
    Random mockRandom = Mock(Random)
    mockRandom.nextInt(_) >>> [0,0,
                              1,1,
                              2,2]
    def pphrase = ["alpha", "bravo", "charlie"]

    when:
    PassphraseGenCli.addRandomCharRepeatedly(mockRandom, "012345", 3, pphrase)

    then:
    ["0", "1", "2", "alpha", "bravo", "charlie"] == pphrase

  }
}
