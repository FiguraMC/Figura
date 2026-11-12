package org.figuramc.figura.font.providers;

public class EmojiBitmapProvider extends EmojiProvider {
	public final String file;
	public final int ascent;
	public final int x;
	public final int y;

	public EmojiBitmapProvider(String type, String file, int ascent, int x, int y) {
		super(type);
		this.file = file;
		this.ascent = ascent;
		this.x = x;
		this.y = y;
	}
}
