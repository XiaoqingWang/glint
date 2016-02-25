package glint.models.server

import akka.actor.{Actor, ActorLogging}
import breeze.linalg.Matrix
import breeze.math.Semiring

import scala.reflect.ClassTag

/**
  * A partial model representing a part of some matrix
  *
  * @param start The row start index
  * @param end The row end index
  * @param cols The number of columns
  * @tparam V The type of value to store
  */
private[glint] abstract class PartialMatrix[@specialized V: Semiring : ClassTag](val start: Long,
                                                                                 val end: Long,
                                                                                 val cols: Int) extends Actor with ActorLogging {

  log.info(s"Constructing PartialMatrix[${implicitly[ClassTag[V]]}] with $cols columns for rows [$start, $end)")

  /**
    * The size of this partial matrix in number of rows
    */
  val rows = (end - start).toInt

  /**
    * The data matrix containing the elements
    */
  val data: Array[Array[V]]

  /**
    * Gets rows from the data matrix
    *
    * @param rows The row indices
    * @return A sequence of values
    */
  def getRows(rows: Array[Long]): Array[V] = {
    var i = 0
    val a = new Array[V](rows.length * cols)
    while (i < rows.length) {
      var j = 0
      System.arraycopy(data(index(rows(i))), 0, a, i * cols, cols)
      /*while (j < cols) {
        a(i * cols + j) = data(index(rows(i)), j)
        j += 1
      }*/
      i += 1
    }
    a
  }

  /**
    * Gets values from the data matrix
    *
    * @param rows The row indices
    * @param cols The column indices
    * @return A sequence of values
    */
  def get(rows: Array[Long], cols: Array[Int]): Array[V] = {
    var i = 0
    val a = new Array[V](rows.length)
    while (i < rows.length) {
      a(i) = data(index(rows(i)))(cols(i))
      i += 1
    }
    a
  }

  /**
    * Obtains the local integer index of a given global key
    *
    * @param key The global key
    * @return The local index in the data array
    */
  @inline
  def index(key: Long): Int = (key - start).toInt

  /**
    * Updates the data of this partial model by aggregating given keys and values into it
    *
    * @param rows The rows
    * @param cols The cols
    * @param values The values
    */
  def update(rows: Array[Long], cols: Array[Int], values: Array[V]): Boolean = {
    var i = 0
    while (i < rows.length) {
      data(index(rows(i)))(cols(i)) = aggregate(data(index(rows(i)))(cols(i)), values(i))
      i += 1
    }
    true
  }

  /**
    * Aggregates to values of type V together
    *
    * @param value1 The first value
    * @param value2 The second value
    * @return The aggregated value
    */
  @inline
  def aggregate(value1: V, value2: V): V

}
