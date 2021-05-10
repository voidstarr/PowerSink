package tv.voidstar.powersink.serializer;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import tv.voidstar.powersink.energy.compat.EnergyType;

public class EnergyTypeSerializer implements TypeSerializer<EnergyType> {

    @Override
    public EnergyType deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException {
        String val = value.getNode("energyType").getString();
        if (val == null || val.isEmpty())
            return null;
        return EnergyType.fromString(val);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable EnergyType obj, @NonNull ConfigurationNode value) throws ObjectMappingException {
        if (obj != null)
            value.getNode("energyType").setValue(obj.toString());
    }
}
