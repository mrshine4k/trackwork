package edn.stratodonut.trackwork.tracks.network;

import com.simibubi.create.foundation.networking.BlockEntityDataPacket;
import edn.stratodonut.trackwork.tracks.blocks.OleoWheelBlockEntity;
import edn.stratodonut.trackwork.tracks.blocks.WheelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Server to client
 */
public final class OleoWheelPacket extends BlockEntityDataPacket<OleoWheelBlockEntity> {
    public final float wheelTravel;
    public final float steeringValue;
    public final float horizontalOffset;
    public final boolean grounded; // Client uses for freespin air decay ramp

    public OleoWheelPacket(FriendlyByteBuf buffer) {
        super(buffer);
        this.wheelTravel = buffer.readFloat();
        this.steeringValue = buffer.readFloat();
        this.horizontalOffset = buffer.readFloat();
        this.grounded = buffer.readBoolean();
    }

    public OleoWheelPacket(BlockPos pos, float wheelTravel, float steeringValue, float horizontalOffset, boolean grounded) {
        super(pos);
        this.wheelTravel = wheelTravel;
        this.steeringValue = steeringValue;
        this.horizontalOffset = horizontalOffset;
        this.grounded = grounded;
    }

    @Override
    protected void writeData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.wheelTravel);
        buffer.writeFloat(this.steeringValue);
        buffer.writeFloat(this.horizontalOffset);
        buffer.writeBoolean(this.grounded);
    }

    @Override
    protected void handlePacket(OleoWheelBlockEntity blockEntity) {
        blockEntity.handlePacket(this);
    }
}
