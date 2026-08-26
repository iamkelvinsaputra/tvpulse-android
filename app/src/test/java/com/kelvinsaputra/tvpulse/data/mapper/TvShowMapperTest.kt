package com.kelvinsaputra.tvpulse.data.mapper

import com.kelvinsaputra.tvpulse.data.remote.dto.ImageDto
import com.kelvinsaputra.tvpulse.data.remote.dto.NetworkDto
import com.kelvinsaputra.tvpulse.data.remote.dto.RatingDto
import com.kelvinsaputra.tvpulse.data.remote.dto.ScheduleDto
import com.kelvinsaputra.tvpulse.data.remote.dto.TvShowDto
import org.junit.Assert.assertEquals
import org.junit.Test

class TvShowMapperTest {

    @Test
    fun `dto maps remote fields into domain semantics`() {
        val dto = TvShowDto(
            id = 42,
            name = "Mapped Show",
            language = "English",
            genres = listOf("Drama", "Comedy"),
            premiered = "2026-05-01",
            schedule = ScheduleDto(time = "21:00", days = listOf("Friday")),
            rating = RatingDto(average = 8.4),
            network = NetworkDto("TV Network"),
            image = ImageDto(medium = "medium", original = "original"),
            summary = "<p>Hello</p>",
        )

        val result = dto.toDomain()

        assertEquals(42L, result.id)
        assertEquals("Mapped Show", result.name)
        assertEquals("original", result.imageUrl)
        assertEquals("Friday · 21:00", result.schedule)
        assertEquals("TV Network", result.network)
        assertEquals(8.4, result.rating)
    }
}
