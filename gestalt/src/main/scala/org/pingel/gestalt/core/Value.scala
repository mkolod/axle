package org.pingel.gestalt.core

class Value(v: String)
extends Comparable[Value]
{

  def compareTo(other: Value): Int =  v.compareTo(other.getV)

  def getV(): String = v
	
  override def toString(): String = v

}

