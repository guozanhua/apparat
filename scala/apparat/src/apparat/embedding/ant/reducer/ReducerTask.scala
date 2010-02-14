package apparat.embedding.ant.reducer

import apparat.embedding.ant.ApparatTask
import apparat.tools.reducer.Reducer

/*
 * This file is part of Apparat.
 * 
 * Apparat is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Apparat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Apparat. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) 2009 Joa Ebert
 * http://www.joa-ebert.com/
 * 
 * User: Patrick Le Clec'h
 * Date: 14 févr. 2010
 * Time: 16:15:10
 */

sealed class ReducerTask extends ApparatTask(Reducer, "reducer") {
	def setDeblock(value: Float) = setArgument("d", value)

	def setQuality(value: Float) = setArgument("q", value)
}