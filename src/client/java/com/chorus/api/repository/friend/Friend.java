
/**
 * Created: 12/8/2024
 */
package com.chorus.api.repository.friend;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@Getter
@ToString
@AllArgsConstructor
public class Friend {
    private final UUID   id;
    private final String name;
}


//~ Formatted by Jindent --- http://www.jindent.com
