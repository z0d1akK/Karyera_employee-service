package com.zodiakk.employeeservice.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailUtils")
class EmailUtilsTest {

    @Nested
    @DisplayName("normalize")
    class Normalize {

        @Test
        @DisplayName("returns null for a null email")
        void shouldReturnNullForNull() {
            assertThat(EmailUtils.normalize(null)).isNull();
        }

        @Test
        @DisplayName("trims and lowercases an email")
        void shouldTrimAndLowercase() {
            assertThat(EmailUtils.normalize("  User.Name@Example.COM  "))
                    .isEqualTo("user.name@example.com");
        }

        @Test
        @DisplayName("lowercases an email without surrounding whitespace")
        void shouldLowercaseWithoutWhitespace() {
            assertThat(EmailUtils.normalize("HR@COMPANY.COM")).isEqualTo("hr@company.com");
        }

        @Test
        @DisplayName("returns an already normalized email unchanged")
        void shouldReturnAlreadyNormalizedEmail() {
            assertThat(EmailUtils.normalize("hr@company.com")).isEqualTo("hr@company.com");
        }

        @Test
        @DisplayName("returns an empty string for an empty email")
        void shouldReturnEmptyForEmptyString() {
            assertThat(EmailUtils.normalize("")).isEmpty();
        }

        @Test
        @DisplayName("returns an empty string for a blank email")
        void shouldReturnEmptyForBlankString() {
            assertThat(EmailUtils.normalize("   ")).isEmpty();
            assertThat(EmailUtils.normalize("\t\n")).isEmpty();
        }

        @Test
        @DisplayName("is idempotent for repeated calls")
        void shouldBeIdempotent() {
            String once = EmailUtils.normalize("  Mixed.Case@Example.Com ");

            assertThat(EmailUtils.normalize(once)).isEqualTo(once);
            assertThat(once).isEqualTo("mixed.case@example.com");
        }

        @Test
        @DisplayName("keeps internal whitespace untouched")
        void shouldKeepInternalWhitespace() {
            assertThat(EmailUtils.normalize(" user @example.com ")).isEqualTo("user @example.com");
        }

        @Test
        @DisplayName("lowercases using Locale.ROOT regardless of the default locale")
        void shouldUseLocaleRoot() {
            Locale original = Locale.getDefault();
            try {
                Locale.setDefault(Locale.forLanguageTag("tr-TR"));

                assertThat(EmailUtils.normalize("I@EXAMPLE.COM")).isEqualTo("i@example.com");
            } finally {
                Locale.setDefault(original);
            }
        }
    }
}
