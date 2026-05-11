async (page) => {
  await page.goto('http://127.0.0.1:5173/connections');
  await page.getByRole('button', { name: '聊天' }).first().click();
  await page.waitForURL(/\/chat\?conversation=/);
  const chatUrl = page.url();
  const chatHeader = await page.locator('main h1').textContent();

  await page.goto('http://127.0.0.1:5173/post/1');
  const actionButtons = await page.locator('main button').all();
  for (const btn of actionButtons) {
    const text = ((await btn.textContent()) || '').trim();
    if (text.includes('浏览') || /^\d+$/.test(text)) {
      await btn.click().catch(() => {});
    }
  }
  const pageText = await page.textContent('body');
  return { chatUrl, chatHeader, hasViewDrawer: pageText?.includes('最近浏览') || pageText?.includes('谁看过') || false };
}
