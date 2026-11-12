package org.figuramc.figura.font.providers;

public class EmojiBitmapProvider extends EmojiProvider {
	public final int x;
	public final int y;
	public final String file;
	public final int ascent;
	public final int height;

	public EmojiBitmapProvider(String type, int x, int y, String file, int ascent, int height) {
		super(type);
		this.x = x;
		this.y = y;
		this.file = file;
		this.ascent = ascent;
		this.height = height;
	}
}
