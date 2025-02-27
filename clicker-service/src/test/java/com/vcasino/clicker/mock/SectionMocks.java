package com.vcasino.clicker.mock;

import com.vcasino.clicker.entity.Section;

public class SectionMocks {
    public static Section getSectionMock() {
        return Section.builder()
                .id(1)
                .name("Social")
                .build();
    }
}
