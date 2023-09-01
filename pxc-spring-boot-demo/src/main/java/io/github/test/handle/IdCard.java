package io.github.test.handle;

import io.github.panxiaochao.sensitive.strategy.AbstractFSensitiveStrategy;

/**
 * <p></p>
 *
 * @author Lypxc
 * @since 2023-08-31
 */
public class IdCard extends AbstractFSensitiveStrategy {
    @Override
    public String handler(String jsonValue) {
        return jsonValue + "adasdasdsadasdasdasdasd";
    }
}
