package axle.ml

/**
 * "performance" returns four measures of classification performance
 * for the given class.
 *
 * They are:
 *
 * 1. Precision
 * 2. Recall
 * 3. Specificity
 * 4. Accuracy
 *
 * See http://en.wikipedia.org/wiki/Precision_and_recall for more information.
 *
 * http://en.wikipedia.org/wiki/F1_score
 *
 */

case class ClassifierPerformance(
  precision: Double,
  recall: Double,
  specificity: Double,
  accuracy: Double) {

  def f1Score() = 2 * (precision * recall) / (precision + recall)

  def fScore(β: Int = 1) =
    (1 + (β * β)) * (precision * recall) / ((β * β * precision) + recall)

  override def toString() = s"""
Precision   $precision
Recall      $recall
Specificity $specificity
Accuracy    $accuracy
F1 Score    $f1Score
"""

}