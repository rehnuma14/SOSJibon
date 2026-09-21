
package com.example.sosjibon.ui.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun GeminiResponseCard(
    response: String,
    modifier: Modifier = Modifier
) {

    if (response.isBlank()) {
        return
    }

    val sections = parseGeminiResponse(response)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = Icons.Outlined.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(
                    modifier = Modifier.padding(start = 10.dp)
                )

                Text(
                    text = "SOS Jibon AI",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            sections.forEach { section ->

                GeminiSection(
                    section = section
                )
            }
        }
    }
}


@Composable
private fun GeminiSection(
    section: GeminiSection
) {

    val title = section.title

    val normalizedTitle = title.uppercase()
    val isSummary =
        normalizedTitle == "SUMMARY"

    val isImportant =
        normalizedTitle == "IMPORTANT"

    val isWarning =
        normalizedTitle == "WHEN TO GET HELP"

    val isConcern =
        normalizedTitle == "POSSIBLE CONCERN"

    val isAction =
        normalizedTitle == "WHAT TO DO"


    /*
     * Section background colors
     */
    val background = when {

        isImportant ->
            Color(0xFFFFE5E5)

        isWarning ->
            Color(0xFFFFF0E0)

        isConcern ->
            Color(0xFFFFF7E6)

        isAction ->
            Color(0xFFEAF7EF)

        else ->
            MaterialTheme.colorScheme.surfaceVariant
    }


    /*
     * Section title colors
     */
    val titleColor = when {
        isSummary ->
            Color(0xFFC62828)

        isImportant ->
            Color(0xFFC62828)

        isWarning ->
            Color(0xFFE65100)

        isConcern ->
            Color(0xFF9A6700)

        isAction ->
            Color(0xFF18794E)

        else ->
            MaterialTheme.colorScheme.primary
    }


    /*
     * BODY TEXT COLOR
     *
     * We intentionally use a strong dark color here.
     * This prevents the Gemini output from becoming
     * white/light text on the pale section backgrounds.
     */
    val bodyTextColor = Color(0xFF1F2937)


    val icon = when {

        isImportant ->
            Icons.Outlined.ErrorOutline

        isWarning ->
            Icons.Outlined.WarningAmber

        isConcern ->
            Icons.Outlined.Info

        else ->
            Icons.Outlined.Info
    }


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = background,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(14.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            /*
             * SECTION HEADER
             */
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = titleColor
                )

                Text(
                    text = title,
                    modifier = Modifier.padding(start = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
            }


            Spacer(
                modifier = Modifier.height(8.dp)
            )


            /*
             * SECTION CONTENT
             */
            section.content.forEachIndexed { index, line ->

                val cleanLine = line.trim()

                if (cleanLine.isNotBlank()) {

                    if (
                        cleanLine.matches(
                            Regex("^\\d+[.)]\\s+.*")
                        )
                    ) {

                        NumberedLine(
                            text = cleanLine,
                            modifier = Modifier.padding(
                                top = if (index == 0) {
                                    0.dp
                                } else {
                                    5.dp
                                }
                            )
                        )

                    } else {

                        Text(
                            text = cleanLine,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,

                            /*
                             * FIXED HIGH-CONTRAST TEXT
                             */
                            color = bodyTextColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun NumberedLine(
    text: String,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        val number =
            text.takeWhile {
                it.isDigit()
            }

        val content =
            text
                .dropWhile {
                    it.isDigit()
                }
                .dropWhile {
                    it == '.' || it == ')'
                }
                .trim()


        /*
         * NUMBER
         */
        Text(
            text = number,
            modifier = Modifier.padding(end = 8.dp),
            fontWeight = FontWeight.Bold,

            /*
             * Keep number clearly visible
             */
            color = MaterialTheme.colorScheme.primary
        )


        /*
         * NUMBERED CONTENT
         */
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,

            /*
             * FIXED HIGH-CONTRAST TEXT
             */
            color = Color(0xFF1F2937)
        )
    }
}


private data class GeminiSection(
    val title: String,
    val content: List<String>
)


private fun parseGeminiResponse(
    response: String
): List<GeminiSection> {

    val lines =
        response
            .replace("\r\n", "\n")
            .lines()
            .map {
                it.trim()
            }


    val sections =
        mutableListOf<GeminiSection>()


    var currentTitle =
        "SUMMARY"


    var currentContent =
        mutableListOf<String>()


    fun saveCurrentSection() {

        if (
            currentContent.any {
                it.isNotBlank()
            }
        ) {

            sections.add(
                GeminiSection(
                    title = currentTitle,
                    content = currentContent.filter {
                        it.isNotBlank()
                    }
                )
            )
        }

        currentContent =
            mutableListOf()
    }


    val knownHeaders =
        setOf(
            "SUMMARY",
            "POSSIBLE CONCERN",
            "WHAT TO DO",
            "WHEN TO GET HELP",
            "IMPORTANT",
            "KEY INFORMATION"
        )


    for (line in lines) {

        if (line.isBlank()) {
            continue
        }


        val normalized =
            line
                .replace(":", "")
                .trim()
                .uppercase()


        if (normalized in knownHeaders) {

            saveCurrentSection()

            currentTitle =
                normalized

        } else {

            currentContent.add(
                cleanDisplayLine(line)
            )
        }
    }


    saveCurrentSection()

    return sections
}


private fun cleanDisplayLine(
    line: String
): String {

    return line
        .replace("###", "")
        .replace("**", "")
        .replace("__", "")
        .replace(
            Regex("^\\s*[•*+-]\\s*"),
            ""
        )
        .trim()
}

