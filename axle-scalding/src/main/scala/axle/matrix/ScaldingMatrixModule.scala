
package axle.matrix

import com.twitter.algebird.{ Group, Field, Monoid }
import com.twitter.scalding._
import com.twitter.scalding.mathematics.{ Matrix => ScaldingMatrix, MatrixProduct }
import axle._
import axle.algebra.FunctionPair
import spire.implicits._
import spire.algebra.Eq

object ScaldingMatrixModule extends ScaldingMatrixModule

/**
 *
 * See https://github.com/twitter/scalding/tree/master/tutorial
 *
 */

trait ScaldingMatrixModule extends MatrixModule {

  type RowT = Int
  type ColT = Int
  type C[T] = Field[T]

  implicit val convertBoolean = Field.boolField
  implicit val convertDouble = Field.doubleField
  implicit val convertInt = ???

  class Matrix[T: C: Eq](_storage: ScaldingMatrix[RowT, ColT, T]) extends MatrixLike[T] {

    // implicit val prod: MatrixProduct[ScaldingMatrix[RowT, ColT, T], ScaldingMatrix[RowT, ColT, T], ScaldingMatrix[RowT, ColT, T]] = ???

    val field = implicitly[Field[T]]

    type S = ScaldingMatrix[RowT, ColT, T]

    def storage = _storage

    implicit val format = (t: T) => t.toString // TODO !!!

    def rows: RowT = ??? // scalding.sizeHint.rows
    def columns: ColT = ???
    def length: RowT = ??? // TODO: actualy RowT x ColT

    def apply(i: Int, j: Int): T = ??? //scalding.elementAt(i, j)

    def apply(rs: Seq[Int], cs: Seq[Int]): Matrix[T] = ???

    def toList: List[T] = ???

    def column(j: Int) = matrix(scalding.getCol(j).toMatrix(0))
    def row(i: Int) = ??? // ??? //scalding.getRow(i)

    def isEmpty: Boolean = ???
    def isRowVector: Boolean = rows === 1
    def isColumnVector: Boolean = columns === 1
    def isVector: Boolean = rows === 1 || columns === 1
    def isSquare: Boolean = rows === columns
    def isScalar: Boolean = rows === 1 && columns === 1

    def dup: MatrixLike[T] = ???
    def negate: MatrixLike[T] = matrix(scalding.mapValues(field.negate))
    def transpose: MatrixLike[T] = matrix(scalding.transpose)
    def diag: MatrixLike[T] = matrix(scalding.diagonal)
    def invert: MatrixLike[T] = ??? // matrix(scalding.inverse)
    def ceil: MatrixLike[T] = ???
    def floor: MatrixLike[T] = ??? // matrix(scalding.mapValues(math.floor))
    def log: MatrixLike[Double] = ???
    def log10: MatrixLike[Double] = ???

    def fullSVD() = ???

    def addScalar(x: T) = matrix(scalding.mapValues(field.plus(_, x)))
    def addAssignment(r: Int, c: Int, v: T): Matrix[T] = ???

    def subtractScalar(x: T) = matrix(scalding.mapValues(field.minus(_, x)))
    def multiplyScalar(x: T) = matrix(scalding.mapValues(field.times(_, x)))
    def divideScalar(x: T) = matrix(scalding.mapValues(field.div(_, x)))
    def mulRow(i: Int, x: T) = ???
    def mulColumn(i: Int, x: T) = ???

    def pow(p: Double) = ???

    def addMatrix(other: Matrix[T]) = matrix(scalding + other.scalding)
    def subtractMatrix(other: Matrix[T]) = matrix(scalding - other.scalding)
    def multiplyMatrix(other: Matrix[T]) = matrix(scalding * other.scalding)

    def mulPointwise(other: Matrix[T]) = ???
    def divPointwise(other: Matrix[T]) = ???

    def concatenateHorizontally(right: Matrix[T]) = ???
    def concatenateVertically(under: Matrix[T]) = ???
    def solve(B: Matrix[T]) = ???

    def addRowVector(row: Matrix[T]) = ???
    def addColumnVector(column: Matrix[T]) = ???
    def subRowVector(row: Matrix[T]) = ???
    def subColumnVector(column: Matrix[T]) = ???
    def mulRowVector(row: Matrix[T]) = ???
    def mulColumnVector(column: Matrix[T]) = ???
    def divRowVector(row: Matrix[T]) = ???
    def divColumnVector(column: Matrix[T]) = ???

    def lt(other: Matrix[T]) = ???
    def le(other: Matrix[T]) = ???
    def gt(other: Matrix[T]) = ???
    def ge(other: Matrix[T]) = ???
    def eq(other: Matrix[T]) = ???
    def ne(other: Matrix[T]) = ???
    def and(other: Matrix[T]) = ???
    def or(other: Matrix[T]) = ???
    def xor(other: Matrix[T]) = ???
    def not: MatrixLike[Boolean] = ???

    def max: MatrixLike[T] = ???
    def argmax = ???
    def min: MatrixLike[T] = ???
    def argmin = ???

    def rowSums() = matrix(scalding.sumRowVectors.toMatrix(0))

    def columnSums() = matrix(scalding.sumColVectors.toMatrix(0))

    // def sum(): T = scalding.sum

    def columnMins() = ???
    def columnMaxs() = ???
    def columnMeans() = ???
    def sortColumns() = ???

    def rowMins() = ???
    def rowMaxs() = ???
    def rowMeans() = ???
    def sortRows() = ???

    // higher order methods

    def map[B: C](f: T => B): Matrix[B] = matrix(scalding.mapValues(f(_)))

    def flatMapColumns[A: C](f: Matrix[T] => Matrix[A]): Matrix[A] = ???

    def scalding() = storage
  }

  object Matrix {

    import axle.Show

    implicit def showMatrix[T: C]: Show[Matrix[T]] = new Show[Matrix[T]] {

      def text(m: Matrix[T]): String = {
        import m._
        (0 until rows).map(i => (0 until columns).map(j => format(converter(mtj.get(i, j)))).mkString(" ")).mkString("\n")
      }
    }

  }

  // methods for creating matrices

  def matrix[T: C](s: ScaldingMatrix[RowT, ColT, T]): Matrix[T] = new Matrix(s)

  def matrix[T: C](r: Int, c: Int, values: Array[T]): Matrix[T] =
    matrix(r, c, (i, j) => values(i * c + j))

  def matrix[T: C](m: Int, n: Int, topleft: => T, left: Int => T, top: Int => T, fill: (Int, Int, T, T, T) => T): Matrix[T] = ???

  def matrix[T: C](m: Int, n: Int, f: (Int, Int) => T): Matrix[T] = ???

  def diag[T: C](row: Matrix[T]): Matrix[T] = {
    assert(row.isRowVector)
    val field = implicitly[C[T]]
    val n = row.columns
    matrix(n, n, (r, c) => if (r === c) row(0, r) else field.zero)
  }

  def zeros[T: C](m: Int, n: Int): Matrix[T] = {
    val field = implicitly[C[T]]
    matrix(m, n, (r, c) => field.zero)
  }

  def ones[T: C](m: Int, n: Int): Matrix[T] = {
    val field = implicitly[C[T]]
    matrix(m, n, (r, c) => field.one)
  }

  def eye[T: C](n: Int): Matrix[T] = {
    val field = implicitly[C[T]]
    matrix(n, n, (r, c) => if (r === c) field.one else field.zero)
  }

  def I[T: C](n: Int): Matrix[T] = eye(n)

  def rand[T: C](m: Int, n: Int): Matrix[T] = {
    val field = implicitly[C[T]]
    matrix(m, n, (r, c) => ???)
  }

  def randn[T: C](m: Int, n: Int): Matrix[T] = ???

  def falses(m: Int, n: Int): Matrix[Boolean] = {
    val field = implicitly[C[Boolean]]
    matrix(m, n, (r, c) => field.zero)
  }

  def trues(m: Int, n: Int): Matrix[Boolean] = {
    val field = implicitly[C[Boolean]]
    matrix(m, n, (r, c) => field.one)
  }

  override def median(m: Matrix[Double]): Matrix[Double] = ???

  def centerRows(m: Matrix[Double]): Matrix[Double] = ???
  def centerColumns(m: Matrix[Double]): Matrix[Double] = ???

  def rowRange(m: Matrix[Double]): Matrix[Double] = ???
  def columnRange(m: Matrix[Double]): Matrix[Double] = ???

  def sumsq(m: Matrix[Double]): Matrix[Double] = ???

  def cov(m: Matrix[Double]): Matrix[Double] = ???

  def std(m: Matrix[Double]): Matrix[Double] = ???

  def zscore(m: Matrix[Double]): Matrix[Double] = ???

  def pca(Xnorm: Matrix[Double], cutoff: Double = 0.95): (Matrix[Double], Matrix[Double]) = ???

  def numComponentsForCutoff(s: Matrix[Double], cutoff: Double): Int = ???

}
