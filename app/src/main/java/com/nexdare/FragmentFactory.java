/*
 *     Copyright 2016 Makoto Consulting Group, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.nexdare;

import android.os.Bundle;

import java.util.Date;

/**
 * Created by sperry on 7/6/16.
 */
public class FragmentFactory {
    public static final String FRAG_ARG_PREFIX = "fragment.argument.";
    public static final String FRAG_ARG_DATE = FRAG_ARG_PREFIX + Date.class.getName();
    public static final String FRAG_ARG_DATE_TYPE = FRAG_ARG_DATE + "Type";
    public static final String FRAG_ARG_DATETIME_PICKER_CHOICE = FRAG_ARG_PREFIX + "datetime.picker.choice";
    public static final String FRAG_ARG_DATETIME_PICKER_RESULT_HANDLER = FRAG_ARG_PREFIX + ".datetime.picker.result.handler";

    public static DateTimePickerFragment createDatePickerFragment(Date date, String dateType, String choice, DateTimePickerFragment.ResultHandler resultHandler) {
        DateTimePickerFragment ret = new DateTimePickerFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAG_ARG_DATE, date);
        args.putSerializable(FRAG_ARG_DATE_TYPE, dateType);
        args.putSerializable(FRAG_ARG_DATETIME_PICKER_CHOICE, choice);
        args.putSerializable(FRAG_ARG_DATETIME_PICKER_RESULT_HANDLER, resultHandler);
        ret.setArguments(args);
        return ret;
    }

    public static DateTimePickerFragment createDatePickerFragment(Date date, String dateType, String choice) {
        return createDatePickerFragment(date, dateType, choice, null);
    }

}
