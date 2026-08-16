package tech.kotlinhero.autohelper.webdriver

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicBoolean

class BrowserSession internal constructor(
    val page: Page,
    private val browser: Browser,
    private val playwright: Playwright,
) : AutoCloseable {

    private val closed = AtomicBoolean(false)

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            runCatching { page.close() }
            runCatching { browser.close() }
            runCatching { playwright.close() }
        }
    }
}

fun browserSession(
    browserExecutablePath: String = "",
    headless: Boolean = false,
): BrowserSession {
    val playwright = Playwright.create()
    return try {
        val launchOptions = BrowserType.LaunchOptions()
            .setHeadless(headless)
            .setArgs(
                listOf(
                    "--no-sandbox",
                    "--disable-setuid-sandbox"
                )
            )

        if (browserExecutablePath.isNotBlank()) {
            launchOptions.setExecutablePath(Path.of(browserExecutablePath))
        }

        val browser = playwright.chromium().launch(launchOptions)
        val context: BrowserContext = browser.newContext(
            Browser.NewContextOptions().setViewportSize(1920, 1080)
        )
        val page: Page = context.newPage()
        page.setDefaultTimeout(30_000.0)

        BrowserSession(page, browser, playwright)
    } catch (t: Throwable) {
        runCatching { playwright.close() }
        throw t
    }
}
