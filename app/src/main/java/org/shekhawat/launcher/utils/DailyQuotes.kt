package org.shekhawat.launcher.utils

import java.time.LocalDate

object DailyQuotes {

    private val quotes = listOf(
        "Almost everything will work again if you unplug it for a few minutes, including you." to "Anne Lamott",
        "The present moment is filled with joy and happiness. If you are attentive, you will see it." to "Thich Nhat Hanh",
        "Breath is the bridge which connects life to consciousness." to "Thich Nhat Hanh",
        "In today's rush, we all think too much, seek too much, want too much, and forget about the joy of just being." to "Eckhart Tolle",
        "Nature does not hurry, yet everything is accomplished." to "Lao Tzu",
        "The greatest weapon against stress is our ability to choose one thought over another." to "William James",
        "Feelings come and go like clouds in a windy sky. Conscious breathing is my anchor." to "Thich Nhat Hanh",
        "Smile, breathe, and go slowly." to "Thich Nhat Hanh",
        "The mind is everything. What you think you become." to "Buddha",
        "Do not dwell in the past, do not dream of the future, concentrate the mind on the present moment." to "Buddha",
        "Peace comes from within. Do not seek it without." to "Buddha",
        "Be where you are, not where you think you should be." to "Unknown",
        "The quieter you become, the more you can hear." to "Ram Dass",
        "Within you, there is a stillness and a sanctuary to which you can retreat at any time." to "Hermann Hesse",
        "Surrender to what is. Let go of what was. Have faith in what will be." to "Sonia Ricotti",
        "You are the sky. Everything else is just the weather." to "Pema Chodron",
        "The best time to relax is when you don't have time for it." to "Sydney J. Harris",
        "Tension is who you think you should be. Relaxation is who you are." to "Chinese Proverb",
        "Set peace of mind as your highest goal, and organize your life around it." to "Brian Tracy",
        "Life is available only in the present moment." to "Thich Nhat Hanh",
        "Simplicity is the ultimate sophistication." to "Leonardo da Vinci",
        "Be happy in the moment, that's enough. Each moment is all we need, not more." to "Mother Teresa",
        "Nothing can bring you peace but yourself." to "Ralph Waldo Emerson",
        "Calmness is the cradle of power." to "Josiah Gilbert Holland",
        "Rest is not idleness, and to lie sometimes on the grass under trees on a summer's day is by no means a waste of time." to "John Lubbock",
        "The time to relax is when you don't have time for it." to "Sydney J. Harris",
        "Your calm mind is the ultimate weapon against your challenges." to "Bryant McGill",
        "When you own your breath, nobody can steal your peace." to "Unknown",
        "Slow down and everything you are chasing will come around and catch you." to "John De Paola",
        "Doing nothing is better than being busy doing nothing." to "Lao Tzu",
        "Stop a moment, cease your work, and look around you." to "Thomas Carlyle",
        "Every breath we take, every step we make, can be filled with peace, joy, and serenity." to "Thich Nhat Hanh",
        "Sometimes the most productive thing you can do is relax." to "Mark Black",
        "Don't underestimate the value of doing nothing." to "A.A. Milne",
        "The soul always knows what to do to heal itself. The challenge is to silence the mind." to "Caroline Myss",
        "To a mind that is still, the whole universe surrenders." to "Lao Tzu",
        "In the midst of movement and chaos, keep stillness inside of you." to "Deepak Chopra",
        "Disconnect to reconnect." to "Unknown",
        "Put your phone down and be present." to "Unknown",
        "The less you respond to negative people, the more peaceful your life will become." to "Unknown",
        "Mindfulness is a way of befriending ourselves and our experience." to "Jon Kabat-Zinn",
        "If you want to conquer the anxiety of life, live in the moment, live in the breath." to "Amit Ray",
        "Happiness is not something ready-made. It comes from your own actions." to "Dalai Lama",
        "What you seek is seeking you." to "Rumi",
        "Let go of the thoughts that don't make you strong." to "Karen Salmansohn",
        "One conscious breath in and out is a meditation." to "Eckhart Tolle",
        "Look at a tree, a flower, a plant. Let your awareness rest upon it. How still they are, how deeply rooted in being." to "Eckhart Tolle",
        "The greatest step towards a life of simplicity is to learn to let go." to "Steve Maraboli",
        "Inhale the future, exhale the past." to "Unknown",
        "You don't always need a plan. Sometimes you just need to breathe, trust, let go, and see what happens." to "Mandy Hale",
        "Take rest; a field that has rested gives a bountiful crop." to "Ovid",
        "There is more to life than increasing its speed." to "Mahatma Gandhi",
        "An over-indulgence of anything, even something as pure as water, can intoxicate." to "Criss Jami",
        "Learn to be calm and you will always be happy." to "Paramahansa Yogananda",
        "When I let go of what I am, I become what I might be." to "Lao Tzu",
        "Quiet the mind, and the soul will speak." to "Ma Jaya Sati Bhagavati",
        "The only way to live is by accepting each minute as an unrepeatable miracle." to "Tara Brach",
        "Be still. Stillness reveals the secrets of eternity." to "Lao Tzu",
        "You are not a drop in the ocean. You are the entire ocean in a drop." to "Rumi",
        "Between stimulus and response there is a space. In that space is our power to choose our response." to "Viktor Frankl",
    )

    /**
     * Returns the quote for today, seeded by day-of-year.
     */
    fun getQuoteForToday(): Pair<String, String> {
        val index = LocalDate.now().dayOfYear % quotes.size
        return quotes[index]
    }

    /**
     * Returns a random quote different from the given one.
     */
    fun getRandomQuote(excludeIndex: Int = -1): Pair<Pair<String, String>, Int> {
        var index: Int
        do {
            index = (Math.random() * quotes.size).toInt()
        } while (index == excludeIndex && quotes.size > 1)
        return quotes[index] to index
    }

    fun getQuoteIndex(): Int {
        return LocalDate.now().dayOfYear % quotes.size
    }
}
