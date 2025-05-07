package net.minecraftforge.common;

import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.UnmodifiableConfigWrapper;
import net.minecraftforge.fml.config.IConfigSpec;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ForgeConfigSpec extends UnmodifiableConfigWrapper<UnmodifiableConfig> implements IConfigSpec<ForgeConfigSpec> {
    protected ForgeConfigSpec(UnmodifiableConfig config) {
        super(config);
    }

    @Override
    public void acceptConfig(com.electronwill.nightconfig.core.CommentedConfig data) {

    }

    @Override
    public boolean isCorrecting() {
        return false;
    }

    @Override
    public boolean isCorrect(com.electronwill.nightconfig.core.CommentedConfig commentedFileConfig) {
        return false;
    }

    @Override
    public int correct(com.electronwill.nightconfig.core.CommentedConfig commentedFileConfig) {
        return 0;
    }

    @Override
    public void afterReload() {

    }

    @Override
    public <T> T getRaw(List<String> path) {
        return null;
    }

    @Override
    public boolean contains(List<String> path) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Map<String, Object> valueMap() {
        return Map.of();
    }

    @Override
    public Set<? extends Entry> entrySet() {
        return Set.of();
    }

    @Override
    public ConfigFormat<?> configFormat() {
        return null;
    }
}
