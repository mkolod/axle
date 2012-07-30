package axle.stats

import collection._
import axle.Enrichments._
import axle.Statistics._

trait Case[A] {
  def and[B](right: Case[B]): Case[(A, B)] = CaseAnd[A, B](this, right)
  def ∧[B](right: Case[B]): Case[(A, B)] = CaseAnd[A, B](this, right)
  def ∩[B](right: Case[B]): Case[(A, B)] = CaseAnd[A, B](this, right)
  def or[B](right: Case[B]): Case[(A, B)] = CaseOr[A, B](this, right)
  def ∨[B](right: Case[B]): Case[(A, B)] = CaseOr[A, B](this, right)
  def ∪[B](right: Case[B]): Case[(A, B)] = CaseOr[A, B](this, right)
  def |[B](given: Case[B]): Case[(A, B)] = CaseGiven[A, B](this, given)
  def probability[B](given: Option[Case[B]] = None): Double
}

case class CaseAndGT[A](conjuncts: GenTraversable[Case[A]]) extends Case[List[A]] {
  def probability[B](given: Option[Case[B]] = None): Double =
    given
      .map(g => conjuncts.Π((c: Case[A]) => () => P(c | g)()))
      .getOrElse(conjuncts.Π((c: Case[A]) => () => P(c)()))
}

case class CaseAnd[A, B](left: Case[A], right: Case[B]) extends Case[(A, B)] {
  def probability[C](given: Option[Case[C]] = None): Double =
    given
      .map(g => P(left | g)() * P(right | g)())
      .getOrElse(P(left)() * P(right)())

}

case class CaseOr[A, B](left: Case[A], right: Case[B]) extends Case[(A, B)] {
  def probability[C](given: Option[Case[C]] = None): Double =
    given
      .map(g => P(left | g)() + P(right | g)() - P(left ∧ right | g)())
      .getOrElse(P(left)() + P(right)() - P(left ∧ right)())

}

// TODO: use phantom types to ensure that only one "given" clause is specified
case class CaseGiven[A, B](c: Case[A], given: Case[B]) extends Case[(A, B)] {
  def probability[C](givenArg: Option[Case[C]] = None): Double = {
    assert(givenArg.isEmpty)
    c.probability(Some(given))
  }
}

// TODO: may want CaseIs{With, No] classes to avoid the run-time type-checking below
case class CaseIs[A](rv: RandomVariable[A], v: A) extends Case[A] {
  def probability[B](given: Option[Case[B]]): Double = rv match {
    case rvNo: RandomVariable0[A] => rvNo.probability(v)
    case rvWith: RandomVariable1[A, B] => given
      .map(g => rvWith.probability(v, g))
      .getOrElse(rvWith.probability(v))
  }
}

case class CaseIsnt[A](rv: RandomVariable[A], v: A) extends Case[A] {
  def probability[B](given: Option[Case[B]] = None): Double = 1.0 - P(rv eq v)()
}
