
/**
 * Created: 12/7/2024
 */
package com.chorus.core.client;

import java.nio.file.Path;

public interface ClientInfoProvider {
    String branch();

    Path clientDir();

    Path configsDir();

    Path filesDir();

    String name();

    String version();

    String getFullInfo();
}


//~ Formatted by Jindent --- http://www.jindent.com
