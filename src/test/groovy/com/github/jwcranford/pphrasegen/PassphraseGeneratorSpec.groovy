package com.github.jwcranford.pphrasegen

import spock.lang.Specification

import java.util.function.Supplier

class PassphraseGeneratorSpec extends Specification {

  def 'generates a passphrase from the given words'() {
    given:
    def supplier = Mock(Supplier)
    supplier.get() >>> ["adam", "bobby", "charlie"]
    def p = new PassphraseGenerator(supplier)

    expect:
    ["adam", "bobby", "charlie"] == p.nextPassphrase(3)
  }
}
