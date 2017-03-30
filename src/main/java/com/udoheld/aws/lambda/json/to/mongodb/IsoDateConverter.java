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

import org.mongodb.morphia.converters.SimpleValueConverter;
import org.mongodb.morphia.converters.TypeConverter;
import org.mongodb.morphia.mapping.MappedField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Custom MongoDB Morphia converter for dates. All timestamps get converted to UTC.
 *
 * @author Udo Held
 */
public class IsoDateConverter extends TypeConverter implements SimpleValueConverter {
  public IsoDateConverter() {
    super(new Class[]{LocalDate.class});
  }

  @Override
  public Object decode(Class<?> outClass, Object obj, MappedField mappedField) {
    if (obj == null) {
      return null;
    }

    if (obj instanceof LocalDate) {
      return obj;
    }

    if (obj instanceof Date) {
      return LocalDateTime.ofInstant(((Date) obj).toInstant(), ZoneId.of("UTC")).toLocalDate();
    }

    throw new IllegalArgumentException("Can't convert to LocalDate from " + obj);
  }

  @Override
  public Object encode(Object value, MappedField optionalExtraInfo) {
    if (value == null) {
      return null;
    }
    LocalDate date = (LocalDate) value;
    return Date.from(date.atStartOfDay()
        .atZone(ZoneId.of("UTC"))
        .toInstant());
  }
}
