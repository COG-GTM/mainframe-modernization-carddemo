import { createFileBackedRepositories } from '../data/fileRepositories.js';
import { createApi } from './api.js';

const port = Number(process.env.PORT ?? 3000);
const app = createApi({ repositories: createFileBackedRepositories() });

app.listen(port, () => {
  console.log(`CardDemo online services listening on http://localhost:${port}`);
});
