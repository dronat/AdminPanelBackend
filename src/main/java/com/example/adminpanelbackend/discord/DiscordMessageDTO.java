package com.example.adminpanelbackend.discord;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class DiscordMessageDTO {
    private String content;
    private List<Embed> embeds;
    private List<String> attachments = new ArrayList<>();

    public DiscordMessageDTO addEmbded(Embed embed) {
        if (embeds == null) {
            embeds = new ArrayList<>();
        }
        embeds.add(embed);
        return this;
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class Embed {
        private long color = 15548997;
        private List<Field> fields;
        //private Author author;
        private String title;
    }

    @Data
    @Accessors(chain = true)
    public static class Field {
        private String name;
        private String value;
    }

    @Data
    @Accessors(chain = true)
    public static class InlineField extends Field {
        private boolean inline;
    }

    /*@Data
    @Accessors(chain = true)
    public static class Author{
        private String name;
    }*/
}
