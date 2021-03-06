
package axle.lx

import axle.Show
import axle.string
import spire.algebra.Eq
import spire.algebra.Order

object Gold {

  type Expression = Iterable[Morpheme]

  object Expression {

    implicit val showExpression: Show[Expression] = new Show[Expression] {
      def text(expr: Expression): String = expr.mkString(" ")
    }

    implicit val orderExpression: Order[Expression] = new Order[Expression] {
      def compare(x: Expression, y: Expression): Int = string(x).compareTo(string(y))
    }
   
  }

  val ♯ = List[Morpheme]()

  trait Grammar {
    def ℒ: Language
  }

  case class HardCodedGrammar(ℒ: Language) extends Grammar

  implicit def enLanguage(sequences: Set[Expression]): Language = Language(sequences)

  case class Language(sequences: Set[Expression])

  object Language {
    
    implicit def showLanguage: Show[Language] = new Show[Language] {
      def text(l: Language): String = "{" + l.sequences.mkString(", ") + "}"
    }
    
    implicit val languageEq = new Eq[Language] {
      def eqv(x: Language, y: Language): Boolean = x.sequences.equals(y.sequences)
    }
  }

  trait Learner[S] {

    def initialState: S

    def processExpression(state: S, expression: Expression): (S, Option[Grammar])

    val noGuess = None.asInstanceOf[Option[Grammar]]

    def guesses(T: Text): Iterator[Grammar] =
      T.expressions.iterator
        .scanLeft((initialState, noGuess))((sg, e) => processExpression(sg._1, e))
        .flatMap(_._2)
  }

  case class HardCodedLearner(G: Grammar) extends Learner[Nothing] {

    def initialState: Nothing = null.asInstanceOf[Nothing]

    def processExpression(state: Nothing, e: Expression): (Nothing, Option[Grammar]) =
      (initialState, Some(G))
  }

  case class MemorizingLearner() extends Learner[Language] {

    def initialState: Language = Language(Set())

    def processExpression(state: Language, expression: Expression): (Language, Option[Grammar]) = {
      val newState = Language(state.sequences ++ List(expression))
      (newState, Some(HardCodedGrammar(newState)))
    }

  }

  case class Morpheme(s: String)
  object Morpheme {
    implicit def showMorpheme: Show[Morpheme] = new Show[Morpheme] {
      def text(m: Morpheme): String = m.s
    }
  }

  case class Text(expressions: List[Expression]) {

    def length: Int = expressions.size

    def isFor(ℒ: Language) = content.equals(ℒ) // TODO equals

    def content: Language = Language(expressions.filter(_ != ♯).toSet)
  }
  object Text {
    implicit def showText: Show[Text] = new Show[Text] {
      def text(t: Text): String = "<" + t.expressions.mkString(", ") + ">"
    }
  }

  implicit def enVocabulary(morphemes: Set[Morpheme]): Vocabulary = Vocabulary(morphemes)

  case class Vocabulary(morphemes: Set[Morpheme]) {

    def iterator: Iterator[Morpheme] = morphemes.iterator
  }

}
