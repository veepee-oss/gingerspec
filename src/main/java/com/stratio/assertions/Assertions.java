package com.stratio.assertions;

import com.stratio.tests.utils.HttpResponse;

public class Assertions extends org.assertj.core.api.Assertions {

    public static HttpResponseAssert assertThat(HttpResponse actual) {
        return new HttpResponseAssert(actual);
    }

}
