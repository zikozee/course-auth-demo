package com.zee.courseauthdemo.util;


import lombok.extern.slf4j.Slf4j;

/**
 * @dev : Ezekiel Eromosei
 * @date : 13 Jun, 2026
 */

@Slf4j
public class IgnoreForProdProfile {

    @Override
    public boolean equals(Object value) {
        if(value == null ) {
            return true;
        }
        return ProfileHolder.isProdActive();
    }

    @Override
    public int hashCode() {
        return 42;
    }
}
