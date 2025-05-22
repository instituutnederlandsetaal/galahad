// Libraries & stores

import stores from "@/stores"
// Types & API
import type { Tagset } from "@/types/tagset"
import * as API from "@/api/tagset"

/**
 * Stores all available tagsets. Mainly informational.
 */
const useTagsets = defineStore("tagsets", () => {
	// Stores
	const app = stores.useApp()

	// Fields
	const loading = ref(false)
	const tagsets = ref([] as Tagset[])

	// Methods
	function reload() {
		loading.value = true
		API.getTagsets()
			.then((response) => (tagsets.value = response.data))
			.catch((error) => app.handleServerError("fetch tagsets", error))
			.finally(() => (loading.value = false))
	}

	reload() // load once

	// Exports
	return {
		loading,
		tagsets,
	}
})

export default useTagsets
