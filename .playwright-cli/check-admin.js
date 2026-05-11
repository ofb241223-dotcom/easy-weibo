async (page) => {
  await page.goto('http://127.0.0.1:5173/admin');
  await page.getByText('Welcome to HNUST Easy WeiBo!').click();
  await page.waitForURL(/\/post\/1$/);
  const postUrl = page.url();
  await page.goto('http://127.0.0.1:5173/admin?tab=posts');
  const hasDetailButton = await page.getByRole('button', { name: '查看详情' }).count();
  return { postUrl, hasDetailButton };
}
