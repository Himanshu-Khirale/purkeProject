/**
 * HMS SaaS — Supabase REST Client
 * Lightweight wrapper for Supabase PostgREST API.
 * No external dependencies — pure vanilla JS fetch().
 */
const SupabaseClient = (() => {
    const SUPABASE_URL = 'https://cpisbazpqkrdzxcacvpq.supabase.co';
    const SUPABASE_ANON_KEY = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImNwaXNiYXpwcWtyZHp4Y2FjdnBxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzUzOTkwMTYsImV4cCI6MjA5MDk3NTAxNn0.XjanUAUC84zPzAErQ__Wz-jOyPJnh73e2tu5AM4RHaU';
    const REST_URL = `${SUPABASE_URL}/rest/v1`;

    // Default tenant ID (City General Hospital)
    const DEFAULT_TENANT_ID = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';

    function headers(extra = {}) {
        return {
            'apikey': SUPABASE_ANON_KEY,
            'Authorization': `Bearer ${SUPABASE_ANON_KEY}`,
            'Content-Type': 'application/json',
            'Prefer': 'return=representation',
            ...extra
        };
    }

    // ── Core REST methods ──

    // Tables that have a deleted_at column for soft-delete filtering
    const SOFT_DELETE_TABLES = new Set([
        'admissions','appointments','departments','doctors','insurance_claims',
        'invoices','lab_orders','medical_history','medical_records','medicines',
        'patient_documents','patients','prescriptions','roles','staff',
        'suppliers','tenants','users'
    ]);

    async function select(table, { columns = '*', filters = {}, order, limit, offset, search, searchColumns, count = false, eq = {}, neq = {} } = {}) {
        let url = `${REST_URL}/${table}?select=${columns}`;

        // Equality filters
        for (const [key, val] of Object.entries(eq)) {
            if (val != null && val !== '') url += `&${key}=eq.${val}`;
        }
        for (const [key, val] of Object.entries(neq)) {
            if (val != null && val !== '') url += `&${key}=neq.${val}`;
        }
        // Generic filters object
        for (const [key, val] of Object.entries(filters)) {
            if (val != null && val !== '') url += `&${key}=${val}`;
        }
        // Text search across columns
        if (search && searchColumns && searchColumns.length > 0) {
            const orParts = searchColumns.map(col => `${col}.ilike.*${search}*`);
            url += `&or=(${orParts.join(',')})`;
        }
        // Soft delete filter — only for tables that have this column
        if (SOFT_DELETE_TABLES.has(table)) {
            url += `&deleted_at=is.null`;
        }
        // Ordering
        if (order) url += `&order=${order}`;
        // Pagination
        if (limit != null) url += `&limit=${limit}`;
        if (offset != null) url += `&offset=${offset}`;

        const hdrs = headers();
        if (count) {
            hdrs['Prefer'] = 'count=exact';
        }

        const res = await fetch(url, { headers: hdrs });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `Select failed: ${res.status}`); }

        const data = await res.json();
        const totalCount = count ? parseInt(res.headers.get('content-range')?.split('/')[1] || '0') : null;

        return { data, totalCount };
    }

    async function insert(table, body) {
        const res = await fetch(`${REST_URL}/${table}`, {
            method: 'POST',
            headers: headers(),
            body: JSON.stringify(body)
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `Insert failed: ${res.status}`); }
        const data = await res.json();
        return data[0] || data;
    }

    async function update(table, id, body) {
        const res = await fetch(`${REST_URL}/${table}?id=eq.${id}`, {
            method: 'PATCH',
            headers: headers(),
            body: JSON.stringify(body)
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `Update failed: ${res.status}`); }
        const data = await res.json();
        return data[0] || data;
    }

    async function remove(table, id) {
        const res = await fetch(`${REST_URL}/${table}?id=eq.${id}`, {
            method: 'DELETE',
            headers: headers()
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `Delete failed: ${res.status}`); }
        return true;
    }

    async function selectOne(table, id, columns = '*') {
        const res = await fetch(`${REST_URL}/${table}?id=eq.${id}&select=${columns}`, {
            headers: headers()
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `Select failed: ${res.status}`); }
        const data = await res.json();
        return data[0] || null;
    }

    async function rpc(fnName, params = {}) {
        const res = await fetch(`${SUPABASE_URL}/rest/v1/rpc/${fnName}`, {
            method: 'POST',
            headers: headers(),
            body: JSON.stringify(params)
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.message || `RPC failed: ${res.status}`); }
        return res.json();
    }

    return {
        select, insert, update, remove, selectOne, rpc,
        SUPABASE_URL, REST_URL, DEFAULT_TENANT_ID, headers
    };
})();
