/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.graph

import annotation.tailrec

class ControlFlowGraph[T, V <: BlockVertex[T]](val graph: GraphLike[V], val entryVertex: V, val exitVertex: V) extends ControlFlowGraphLike[V] with DOTExportAvailable[V] {
	override type G = ControlFlowGraph[T, V]

	type ControlFlowVertex = V
	type ControlFlowEdge = E
	type ControlFlowElm = T

	override def topsort = graph.topsort

	override def sccs = graph.sccs

	override def dominance = graph.dominance

	override def predecessorsOf(vertex: V) = graph.predecessorsOf(vertex)

	override def successorsOf(vertex: V) = graph.successorsOf(vertex)

	override def incomingOf(vertex: V) = graph.incomingOf(vertex)

	override def verticesIterator = graph.verticesIterator

	override def edgesIterator = graph.edgesIterator

	override def indegreeOf(vertex: V) = graph.indegreeOf(vertex)

	override def outdegreeOf(vertex: V) = graph.outdegreeOf(vertex)

	override def contains(edge: E) = graph.contains(edge)

	override def outgoingOf(vertex: V) = graph.outgoingOf(vertex)

	override def contains(vertex: V) = graph.contains(vertex)

	override def +(edge: E) = new G(graph + edge, entryVertex, exitVertex)

	override def -(edge: E) = new G(graph - edge, entryVertex, exitVertex)

	override def +(vertex: V) = new G(graph + vertex, entryVertex, exitVertex)

	override def -(vertex: V) = new G(graph - vertex, entryVertex, exitVertex)

	override def replace(v0: V, v1: V) = new G(graph.replace(v0, v1), entryVertex, exitVertex)

	override def optimized = simplified

	override def toString = "[ControlFlowGraph]"

	private lazy val simplified = {
		var g = graph
		var modified = false
		@tailrec def loop() {
			var vertices = g.verticesIterator.filterNot(p => p == entryVertex || p == exitVertex)

			//remove empty block
			vertices.filter(_.isEmpty).foreach {
				emptyVertex =>
					val out = g.outgoingOf(emptyVertex)
					if (out.size == 1 && out.head.kind == EdgeKind.Jump) {
						val endEdge = out.head
						g = g - endEdge
						g.incomingOf(emptyVertex).foreach {
							startEdge => g = (g - startEdge) + Edge.copy[V](startEdge, Some(startEdge.startVertex), Some(endEdge.endVertex))
						}
						g = g - emptyVertex

						modified = true
					}
			}

			// remove dead edge
			for (edge <- g.edgesIterator.filter(e => if (g.contains(e.startVertex)) {g.incomingOf(e.startVertex).isEmpty && !isEntry(e.startVertex)} else false)) {
				g = g - edge
				g = g - edge.startVertex

				modified = true
			}

			if (modified) {
				modified = false
				loop()
			}
		}
		loop()

		if (g != graph)
			new G(g, entryVertex, exitVertex)
		else
			this
	}

	def cleanString(str: String) = {
		val len = str.length
		@tailrec def loop(sb: StringBuilder, strIndex: Int): StringBuilder = {
			if (strIndex >= len)
				sb
			else {
				str(strIndex) match {
					case '"' => sb append "\\\""
					case '>' => sb append "&gt;"
					case '<' => sb append "&lt;"
					case '\r' => sb append "\\r"
					case '\t' => sb append "\\t"
					case '\n' => sb append "\\n"
					case c => sb append c
				}
				loop(sb, strIndex + 1)
			}
		}
		loop(new StringBuilder(), 0) toString
	}

	def label(value: String) = "label=\"" + cleanString(value) + "\""

	def vertexToString(vertex: V) = "[" + label({
		if (isEntry(vertex))
			"Entry"
		else if (isExit(vertex))
			"Exit"
		else
			vertex toString
	}) + "]"

	def edgeToString(edge: E) = "[" + label(edge match {
		case DefaultEdge(x, y) => ""
		case JumpEdge(x, y) => "jump"
		case TrueEdge(x, y) => "true"
		case FalseEdge(x, y) => "false"
		case DefaultCaseEdge(x, y) => "default"
		case CaseEdge(x, y) => "case"
		case NumberedCaseEdge(x, y, n) => "case " + n
		case ThrowEdge(x, y) => "throw"
		case ReturnEdge(x, y) => "return"
	}) + "]"

	override def dotExport = {
		new DOTExport(this, (vertex: V) => vertexToString(vertex), (edge: E) => edgeToString(edge))
	}
}
