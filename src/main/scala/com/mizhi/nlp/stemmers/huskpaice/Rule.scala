package com.mizhi.nlp.stemmers.huskpaice

import com.mizhi.nlp.stemmers.huskpaice.RuleAction.RuleAction

case class Rule(suffix: String, append: Option[String], intact: Boolean, nextAction: RuleAction) extends RuleExecutor {
  def this(suffix: String, append: Option[String], action: RuleAction) = this(suffix, append, false, action)

  override def execute(state: ExecutionState): ExecutionState = {
    lazy val stemmed = applyStringTransform(state.word.text)
    if (ruleApplies(state.word) && stemAcceptable(stemmed)) {
      ExecutionState(Word(stemmed, false), Some(nextAction))
    } else {
      state
    }
  }

  // The original algorithm specified the transforms using a deletion amount
  // and subsequent append. This algorithm uses some of the nicer string
  // processing functions we have available these days.
  protected[huskpaice] def applyStringTransform(stem: String): String = {
    append.fold(stem)(stem.stripSuffix(suffix).concat)
  }

  protected[huskpaice] def ruleApplies(word: Word): Boolean = {
    endingMatches(word.text) && intactnessIsGood(word)
  }

  // A Rule only applies if the word has a matching suffix
  protected[huskpaice] def endingMatches(stem: String) = stem.endsWith(suffix)

  // Husk-Paice won't allow a Rule to apply if it mandates the word is intact at the start
  protected[huskpaice] def intactnessIsGood(word: Word) = !intact || (intact && word.intact)

  // There are two oddball acceptability requirements.
  // 1. if starts with a vowel, then must contain two letters after stemming
  // 2. if starts with consonant, then must contain three letters after stemming and
  // one must be vowel or y
  protected val Vowels = Set('a', 'e', 'i', 'o', 'u')
  protected val VowelsAndY = Vowels + 'y'
  protected[huskpaice] def stemAcceptable(word: String): Boolean = {
    word.headOption.fold(false)(_ match {
      case x if Vowels.contains(x) => word.length >= 2
      case x => (word.length >= 3) && (word.count(VowelsAndY) > 0)
    })
  }
}
