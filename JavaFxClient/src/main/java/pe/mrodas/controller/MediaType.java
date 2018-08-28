package pe.mrodas.controller;

import lombok.Getter;

enum MediaType {
    IMAGE("jpg, jpeg, gif, png, bmp"),
    VIDEO("mp4, flv, 3gp"),
    AUDIO("mp3"),
    NONE("");
    @Getter
    private final String extensions;

    MediaType(String extensions) {
        this.extensions = extensions;
    }

    public static MediaType get(String mediaStr) {
        if (mediaStr != null) {
            for (MediaType media : MediaType.values()) {
                if (media.name().equalsIgnoreCase(mediaStr)) {
                    return media;
                }
            }
        }
        return NONE;
    }

}
