/*
    Copyright 2017 the original author or authors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this program. If not, see
    <http://www.gnu.org/licenses/>.
 */

package com.udoheld.aws.lambda.json.to.mongodb;

import org.junit.Test;

import java.time.LocalDate;

import static junit.framework.TestCase.assertTrue;

/**
 * @author Udo Held
 */
public class TestTimestampConversion {

  @Test
  public void testTimestamp (){
    // 2016-01-01T00:01:00Z
    LocalDate localDate = ProcessDataHandler.convertTimestampToDate(1451606460);
    assertTrue(localDate.equals(LocalDate.of(2016,1,1)));

    localDate = ProcessDataHandler.convertTimestampToDate(1451606460 - 80);
    assertTrue(localDate.equals(LocalDate.of(2015,12,31)));

    // 2016-01-01T23:59:00Z
    localDate = ProcessDataHandler.convertTimestampToDate(1451692740);
    assertTrue(localDate.equals(LocalDate.of(2016,1,1)));

    localDate = ProcessDataHandler.convertTimestampToDate(1451692740 + 80);
    assertTrue(localDate.equals(LocalDate.of(2016,1,2)));
  }
}
