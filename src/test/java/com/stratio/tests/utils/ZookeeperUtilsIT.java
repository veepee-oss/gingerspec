package com.stratio.tests.utils;


import com.stratio.exceptions.DBException;
import com.stratio.specs.BaseGSpec;
import com.stratio.specs.CommonG;
import com.stratio.specs.GivenGSpec;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class ZookeeperUtilsIT extends BaseGSpec{
    GivenGSpec commonspecG;

    public ZookeeperUtilsIT() {
        ThreadProperty.set("class", this.getClass().getCanonicalName());
        this.commonspec = new CommonG();
        commonspecG = new GivenGSpec(this.commonspec);
    }

}
