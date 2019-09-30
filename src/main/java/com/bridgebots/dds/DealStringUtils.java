package com.bridgebots.dds;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class DealStringUtils {

    public static List<Rank> parseSuitString(String suitString){
        return Arrays.stream(StringUtils.split(suitString)).map(Rank::parseRank).collect(Collectors.toList());
    }
}
