package net.tslat.aoawikihelpermod.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.FastColor;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NativeImageGifWriter {
	protected final ImageWriter writer;
	protected final ImageWriteParam params;
	protected final IIOMetadata meta;
	protected final int defaultFrameTime;

	private boolean closed = false;

	protected final ArrayList<BufferedImage> frames = new ArrayList<BufferedImage>();

	public NativeImageGifWriter(File outputFile) throws IOException {
		this(outputFile, 5);
	}

	public NativeImageGifWriter(File outputFile, int defaultFrameTime) throws IOException {
		this.writer = ImageIO.getImageWritersBySuffix("gif").next();
		this.params = writer.getDefaultWriteParam();
		this.meta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), this.params);
		this.defaultFrameTime = defaultFrameTime;

		setupDefaultMetadata(this.meta, this.defaultFrameTime);

		if (outputFile.exists())
			outputFile.delete();

		outputFile.getParentFile().mkdirs();

		this.writer.setOutput(new FileImageOutputStream(outputFile));
	}

	public void writeFrame(NativeImage frame) throws IOException {
		BufferedImage bufferedFrame = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < frame.getWidth(); x++) {
			for (int y = 0; y < frame.getHeight(); y++) {
				int rgba = frame.getPixelRGBA(x, y);
				int alpha = FastColor.ABGR32.alpha(rgba);
				int red = FastColor.ABGR32.red(rgba);
				int green = FastColor.ABGR32.green(rgba);
				int blue = FastColor.ABGR32.blue(rgba);

				bufferedFrame.setRGB(x, y, ((alpha & 255) << 24 | (red & 255) << 16 | (green & 255) << 8) | (blue & 255) << 0);
			}
		}

		this.frames.add(bufferedFrame);
	}

	private IIOMetadataNode setupDefaultMetadata(IIOMetadata meta, int frameTime) throws IIOInvalidTreeException {
		String metaFormatName = meta.getNativeMetadataFormatName();
		IIOMetadataNode metaRootNode = (IIOMetadataNode)meta.getAsTree(metaFormatName);

		IIOMetadataNode graphicsControlExtensionNode = getAndAppendNode(metaRootNode, "GraphicControlExtension");
		graphicsControlExtensionNode.setAttribute("disposalMethod", "restoreToBackgroundColor");
		graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("transparentColorFlag", "TRUE");
		graphicsControlExtensionNode.setAttribute("delayTime", String.valueOf(frameTime));
		graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

		IIOMetadataNode appExtensionsNode = getAndAppendNode(metaRootNode, "ApplicationExtensions");
		IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");

		child.setAttribute("applicationID", "NETSCAPE");
		child.setAttribute("authenticationCode", "2.0");
		child.setUserObject(new byte[] {1, 0, 0});

		appExtensionsNode.appendChild(child);

		meta.setFromTree(metaFormatName, metaRootNode);

		return metaRootNode;
	}

	private static IIOMetadataNode getAndAppendNode(IIOMetadataNode rootNode, String nodeName) {
		int nodeCount = rootNode.getLength();

		for (int i = 0; i < nodeCount; i++){
			if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName))
				return (IIOMetadataNode) rootNode.item(i);
		}

		IIOMetadataNode node = new IIOMetadataNode(nodeName);

		rootNode.appendChild(node);

		return node;
	}

	private boolean areFramesEqual(BufferedImage frame1, BufferedImage frame2) {
		if (frame1.getWidth() != frame2.getWidth())
			return false;

		if (frame1.getHeight() != frame2.getHeight())
			return false;

		for (int x = 0; x < frame1.getWidth(); x++) {
			for (int y = 0; y < frame1.getHeight(); y++) {
				if (frame1.getRGB(x, y) != frame2.getRGB(x, y))
					return false;
			}
		}

		return true;
	}

	public void close() {
		if (this.frames.isEmpty()) {
			try {
				((FileImageOutputStream)this.writer.getOutput()).close();

				this.closed = true;
			}
			catch (IOException ignored) {}

			return;
		}

		try {
			this.writer.prepareWriteSequence(this.meta);

			boolean firstFrame = true;
			BufferedImage lastFrame = this.frames.get(0);
			int nextFrameTime = 0;

			for (BufferedImage frame : this.frames) {
				IIOMetadata frameMeta = this.meta;

				if (areFramesEqual(lastFrame, frame) && !firstFrame) {
					nextFrameTime += this.defaultFrameTime;

					continue;
				}
				else if (nextFrameTime > this.defaultFrameTime) {
					frameMeta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), this.params);

					setupDefaultMetadata(frameMeta, nextFrameTime);
					nextFrameTime = this.defaultFrameTime;
					firstFrame = false;
				}

				this.writer.writeToSequence(new IIOImage(lastFrame, null, frameMeta), this.params);

				lastFrame = frame;
			}

			if (nextFrameTime > this.defaultFrameTime) {
				IIOMetadata frameMeta = writer.getDefaultImageMetadata(ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_ARGB), this.params);

				setupDefaultMetadata(frameMeta, nextFrameTime);
				this.writer.writeToSequence(new IIOImage(this.frames.get(this.frames.size() - 1), null, frameMeta), this.params);
			}

			this.writer.endWriteSequence();
			((FileImageOutputStream)this.writer.getOutput()).close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		this.closed = true;
	}

	public void exit() {
		if (closed)
			return;

		try {
			((FileImageOutputStream)this.writer.getOutput()).close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}

		this.closed = true;
	}
}
