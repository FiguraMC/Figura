package org.figuramc.figura.font.providers;

public class EmojiSpaceProvider extends EmojiProvider {
	public final int advance;

	public EmojiSpaceProvider(String type, int advance) {
		super(type);
		this.advance = advance;
	}
}
