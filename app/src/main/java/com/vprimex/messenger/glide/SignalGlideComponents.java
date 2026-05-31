package com.vprimex.messenger.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.UnitModelLoader;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder;
import com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.StreamGifDecoder;

import org.signal.apng.ApngDecoder;
import org.signal.blurhash.BlurHash;
import org.signal.glide.load.resource.apng.decode.APNGDecoder;
import org.signal.glide.blurhash.BlurHashModelLoader;
import org.signal.glide.blurhash.BlurHashResourceDecoder;
import org.signal.glide.common.io.InputStreamFactory;
import com.vprimex.messenger.badges.load.BadgeLoader;
import com.vprimex.messenger.badges.load.GiftBadgeModel;
import com.vprimex.messenger.badges.models.Badge;
import com.vprimex.messenger.contacts.avatars.ContactPhoto;
import com.vprimex.messenger.contacts.avatars.ContactPhotoLoader;
import com.vprimex.messenger.crypto.AttachmentSecret;
import com.vprimex.messenger.crypto.AttachmentSecretProvider;
import com.vprimex.messenger.giph.model.ChunkedImageUrl;
import com.vprimex.messenger.glide.cache.ApngDrawableTranscoder;
import com.vprimex.messenger.glide.cache.ApngFrameDrawableTranscoder;
import com.vprimex.messenger.glide.cache.ApngInputStreamFactoryResourceDecoder;
import com.vprimex.messenger.glide.cache.EncryptedApngCacheDecoder;
import com.vprimex.messenger.glide.cache.ByteBufferApngDecoder;
import com.vprimex.messenger.glide.cache.EncryptedApngCacheEncoder;
import com.vprimex.messenger.glide.cache.EncryptedApngResourceEncoder;
import com.vprimex.messenger.glide.cache.EncryptedBitmapResourceEncoder;
import com.vprimex.messenger.glide.cache.EncryptedCacheDecoder;
import com.vprimex.messenger.glide.cache.EncryptedCacheEncoder;
import com.vprimex.messenger.glide.cache.EncryptedGifDrawableResourceEncoder;
import com.vprimex.messenger.glide.cache.InputStreamFactoryBitmapDecoder;
import com.vprimex.messenger.glide.cache.StreamApngDecoder;
import com.vprimex.messenger.glide.cache.StreamBitmapDecoder;
import com.vprimex.messenger.glide.cache.StreamFactoryApngDecoder;
import com.vprimex.messenger.glide.cache.StreamFactoryGifDecoder;
import com.vprimex.messenger.glide.cache.WebpSanDecoder;
import org.signal.glide.decryptableuri.DecryptableUri;
import org.signal.glide.decryptableuri.DecryptableUriStreamLoader;
import com.vprimex.messenger.mms.RegisterGlideComponents;
import com.vprimex.messenger.mms.SignalGlideModule;
import com.vprimex.messenger.util.RemoteConfig;
import com.vprimex.messenger.stickers.StickerRemoteUri;
import com.vprimex.messenger.stickers.StickerRemoteUriLoader;
import com.vprimex.messenger.stories.StoryTextPostModel;
import com.vprimex.messenger.util.ConversationShortcutPhoto;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * The core logic for {@link SignalGlideModule}. This is a separate class because it uses
 * dependencies defined in the main Gradle module.
 */
public class SignalGlideComponents implements RegisterGlideComponents {

  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    AttachmentSecret attachmentSecret = AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret();
    byte[]           secret           = attachmentSecret.getModernKey();

    registry.prepend(File.class, File.class, UnitModelLoader.Factory.getInstance());

    registry.prepend(InputStream.class, Bitmap.class, new WebpSanDecoder());

    registry.prepend(InputStream.class, new EncryptedCacheEncoder(secret, glide.getArrayPool()));

    registry.prepend(File.class, Bitmap.class, new EncryptedCacheDecoder<>(secret, new StreamBitmapDecoder(context, glide, registry)));

    StreamGifDecoder        streamGifDecoder        = new StreamGifDecoder(registry.getImageHeaderParsers(), new ByteBufferGifDecoder(context, registry.getImageHeaderParsers(), glide.getBitmapPool(), glide.getArrayPool()), glide.getArrayPool());
    StreamFactoryGifDecoder streamFactoryGifDecoder = new StreamFactoryGifDecoder(streamGifDecoder);
    registry.prepend(InputStream.class, GifDrawable.class, streamGifDecoder);
    registry.prepend(InputStreamFactory.class, GifDrawable.class, streamFactoryGifDecoder);
    registry.prepend(GifDrawable.class, new EncryptedGifDrawableResourceEncoder(secret));
    registry.prepend(File.class, GifDrawable.class, new EncryptedCacheDecoder<>(secret, streamGifDecoder));

    EncryptedBitmapResourceEncoder encryptedBitmapResourceEncoder = new EncryptedBitmapResourceEncoder(secret);
    registry.prepend(Bitmap.class, new EncryptedBitmapResourceEncoder(secret));
    registry.prepend(BitmapDrawable.class, new BitmapDrawableEncoder(glide.getBitmapPool(), encryptedBitmapResourceEncoder));


    if (RemoteConfig.newApngRenderer()) {
      registry.prepend(InputStreamFactory.class, ApngDecoder.class, new ApngInputStreamFactoryResourceDecoder());
      registry.prepend(ApngDecoder.class, new EncryptedApngResourceEncoder(secret));
      registry.prepend(File.class, ApngDecoder.class, new EncryptedApngCacheDecoder(secret));
      registry.register(ApngDecoder.class, Drawable.class, new ApngDrawableTranscoder());
    } else {
      ByteBufferApngDecoder    byteBufferApngDecoder    = new ByteBufferApngDecoder();
      StreamApngDecoder        streamApngDecoder        = new StreamApngDecoder(byteBufferApngDecoder);
      StreamFactoryApngDecoder streamFactoryApngDecoder = new StreamFactoryApngDecoder(byteBufferApngDecoder, glide, registry);

      registry.prepend(InputStream.class, APNGDecoder.class, streamApngDecoder);
      registry.prepend(InputStreamFactory.class, APNGDecoder.class, streamFactoryApngDecoder);
      registry.prepend(ByteBuffer.class, APNGDecoder.class, byteBufferApngDecoder);
      registry.prepend(APNGDecoder.class, new EncryptedApngCacheEncoder(secret));
      registry.prepend(File.class, APNGDecoder.class, new EncryptedCacheDecoder<>(secret, streamApngDecoder));
      registry.register(APNGDecoder.class, Drawable.class, new ApngFrameDrawableTranscoder());
    }

    registry.prepend(BlurHash.class, Bitmap.class, new BlurHashResourceDecoder());
    registry.prepend(StoryTextPostModel.class, Bitmap.class, new StoryTextPostModel.Decoder());

    registry.append(StoryTextPostModel.class, StoryTextPostModel.class, UnitModelLoader.Factory.getInstance());
    registry.append(ConversationShortcutPhoto.class, Bitmap.class, new ConversationShortcutPhoto.Loader.Factory(context));
    registry.append(ContactPhoto.class, InputStream.class, new ContactPhotoLoader.Factory(context));
    registry.append(DecryptableUri.class, InputStreamFactory.class, new DecryptableUriStreamLoader.Factory(context));
    registry.append(InputStreamFactory.class, Bitmap.class, new InputStreamFactoryBitmapDecoder(context, glide, registry));
    registry.append(ChunkedImageUrl.class, InputStream.class, new ChunkedImageUrlLoader.Factory());
    registry.append(StickerRemoteUri.class, InputStream.class, new StickerRemoteUriLoader.Factory());
    registry.append(BlurHash.class, BlurHash.class, new BlurHashModelLoader.Factory());
    registry.append(Badge.class, InputStream.class, BadgeLoader.createFactory());
    registry.append(GiftBadgeModel.class, InputStream.class, GiftBadgeModel.createFactory());
    registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
  }
}
