async (page) => {
  await page.goto('http://127.0.0.1:5173/');
  await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
  const before = await page.evaluate(() => window.scrollY);
  await page.getByRole('button', { name: /正在热议 #MySQL/ }).click();
  await page.waitForURL(/\/topics\?tag=MySQL/);
  await page.waitForTimeout(500);
  const after = await page.evaluate(() => window.scrollY);
  return { before, after, url: page.url() };
}
