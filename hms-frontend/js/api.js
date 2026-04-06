/**
 * HMS SaaS — API Client (Supabase Direct Mode)
 * Talks directly to Supabase REST API. No backend needed.
 * Requires supabase-client.js to be loaded first.
 */
const HMS = (() => {
    const TENANT_ID = SupabaseClient.DEFAULT_TENANT_ID;

    // ── Token / Auth Management ──
    function getToken()  { return localStorage.getItem('hms_access_token'); }
    function getRefresh(){ return localStorage.getItem('hms_refresh_token'); }
    function getUser()   { try { return JSON.parse(localStorage.getItem('hms_user')); } catch { return null; } }

    function saveAuth(data) {
        localStorage.setItem('hms_access_token', data.accessToken || 'supabase-direct');
        localStorage.setItem('hms_refresh_token', data.refreshToken || 'supabase-direct');
        localStorage.setItem('hms_user', JSON.stringify({
            userId: data.userId, email: data.email,
            firstName: data.firstName, lastName: data.lastName,
            tenantId: data.tenantId || TENANT_ID,
            tenantName: data.tenantName || 'City General Hospital',
            roles: data.roles || ['ADMIN'], permissions: data.permissions || []
        }));
    }

    function clearAuth() {
        localStorage.removeItem('hms_access_token');
        localStorage.removeItem('hms_refresh_token');
        localStorage.removeItem('hms_user');
    }

    function isLoggedIn() { return !!getToken(); }

    function requireAuth() {
        if (!isLoggedIn()) { window.location.href = 'login.html'; return false; }
        return true;
    }

    // ── Route parsing helper ──
    function parsePath(path) {
        const parts = path.replace(/^\/+/, '').split('/').filter(Boolean);
        return parts;
    }

    // ── GET handler ──
    async function get(path, params = {}) {
        const parts = parsePath(path);

        // ─── Dashboard ───
        if (parts[0] === 'dashboard' && parts[1] === 'stats') {
            return getDashboardStats();
        }

        // ─── Billing ───
        if (parts[0] === 'billing') {
            if (parts[1] === 'stats') return getBillingStats();
            if (parts[1] === 'invoices') {
                if (parts[2]) return getOne('invoices', parts[2], 'billing');
                return getList('invoices', params, 'billing');
            }
            if (parts[1]) return getOne('invoices', parts[1], 'billing');
            return getList('invoices', params, 'billing');
        }

        // ─── Pharmacy ───
        if (parts[0] === 'pharmacy') {
            if (parts[1] === 'inventory') {
                if (parts[2]) return getOne('medicines', parts[2], 'pharmacy');
                return getPharmacyList(params);
            }
            if (parts[1]) return getOne('medicines', parts[1], 'pharmacy');
            return getPharmacyList(params);
        }

        // ─── Lab ───
        if (parts[0] === 'lab') {
            if (parts[1] === 'orders') {
                if (parts[2]) return getOne('lab_orders', parts[2], 'lab');
                return getList('lab_orders', params, 'lab');
            }
            if (parts[1]) return getOne('lab_orders', parts[1], 'lab');
            return getList('lab_orders', params, 'lab');
        }

        // ─── EMR ───
        if (parts[0] === 'emr') {
            if (parts[1] === 'patients' && parts[3] === 'records') {
                return getPatientRecords(parts[2]);
            }
            if (parts[1] === 'records') {
                if (parts[2]) return getOne('medical_records', parts[2], 'emr');
                return getList('medical_records', params, 'emr');
            }
            if (parts[1]) return getOne('medical_records', parts[1], 'emr');
            return getList('medical_records', params, 'emr');
        }

        // ─── Inpatient ───
        if (parts[0] === 'inpatient') {
            if (parts[1] === 'beds') {
                if (parts[2]) return getOne('beds', parts[2], 'beds');
                return getBedsWithStatus();
            }
            if (parts[1] === 'admissions') {
                if (parts[2]) return getOne('admissions', parts[2], 'inpatient');
                return getList('admissions', params, 'inpatient');
            }
            return getBedsWithStatus();
        }

        // ─── Standard resources ───
        const resource = parts[0];
        const id = parts[1];
        const table = mapTable(resource);
        if (id) return getOne(table, id, resource);
        return getList(table, params, resource);
    }

    // ── POST handler ──
    async function post(path, body) {
        const parts = parsePath(path);

        // Auth
        if (parts[0] === 'auth') {
            if (parts[1] === 'login') return handleLogin(body);
            if (parts[1] === 'logout') { clearAuth(); return { success: true }; }
            if (parts[1] === 'refresh') return { data: { accessToken: getToken() }, success: true };
        }

        // Billing
        if (parts[0] === 'billing') {
            return postToTable('invoices', body, 'billing');
        }

        // Pharmacy
        if (parts[0] === 'pharmacy') {
            return postToTable('medicines', body, 'pharmacy');
        }

        // Lab
        if (parts[0] === 'lab') {
            return postToTable('lab_orders', body, 'lab');
        }

        // EMR
        if (parts[0] === 'emr') {
            return postToTable('medical_records', body, 'emr');
        }

        // Inpatient — bed assignment
        if (parts[0] === 'inpatient') {
            return admitPatient(body);
        }

        // Standard resource
        const resource = parts[0];
        const table = mapTable(resource);
        return postToTable(table, body, resource);
    }

    // ── PUT handler ──
    async function put(path, body) {
        const parts = parsePath(path);
        let resource = parts[0], id = parts[1], table;

        if (resource === 'billing') { table = 'invoices'; resource = 'billing'; id = parts[2] || parts[1]; }
        else if (resource === 'pharmacy') { table = 'medicines'; resource = 'pharmacy'; id = parts[2] || parts[1]; }
        else if (resource === 'lab') { table = 'lab_orders'; resource = 'lab'; id = parts[2] || parts[1]; }
        else if (resource === 'emr') { table = 'medical_records'; resource = 'emr'; id = parts[2] || parts[1]; }
        else { table = mapTable(resource); }

        const data = mapToSupabase(resource, body);
        delete data.tenant_id;
        delete data.id;
        data.updated_at = new Date().toISOString();
        const result = await SupabaseClient.update(table, id, data);
        return { data: mapFromSupabase(resource, result), success: true };
    }

    async function patch(path, body) { return put(path, body); }

    // ── DELETE handler ──
    async function del(path) {
        const parts = parsePath(path);
        let resource = parts[0], id;

        // /inpatient/beds/{id}/release — discharge
        if (resource === 'inpatient' && parts[1] === 'beds' && parts[3] === 'release') {
            return dischargePatient(parts[2]);
        }

        if (resource === 'billing') { id = parts[2] || parts[1]; await SupabaseClient.remove('invoices', id); }
        else if (resource === 'pharmacy') { id = parts[2] || parts[1]; await SupabaseClient.remove('medicines', id); }
        else if (resource === 'lab') { id = parts[2] || parts[1]; await SupabaseClient.remove('lab_orders', id); }
        else if (resource === 'emr') { id = parts[2] || parts[1]; await SupabaseClient.remove('medical_records', id); }
        else { id = parts[1]; await SupabaseClient.remove(mapTable(resource), id); }

        return { success: true };
    }

    // ── Standard get-one ──
    async function getOne(table, id, resource) {
        const item = await SupabaseClient.selectOne(table, id, selectColumns(resource));
        return { data: mapFromSupabase(resource, item), success: true };
    }

    // ── Standard get-list ──
    async function getList(table, params, resource) {
        const page = parseInt(params.page) || 0;
        const size = parseInt(params.size) || 20;
        const search = params.search || params.q || null;
        const sortBy = params.sortBy || 'created_at';
        const sortDir = params.sortDir || 'desc';

        const result = await SupabaseClient.select(table, {
            columns: selectColumns(resource),
            order: `${mapColumn(sortBy)}.${sortDir}`,
            limit: size,
            offset: page * size,
            search: search,
            searchColumns: searchColumnsFor(resource),
            count: true
        });

        const totalElements = result.totalCount || result.data.length;
        const totalPages = Math.ceil(totalElements / size) || 1;

        return {
            data: {
                content: result.data.map(row => mapFromSupabase(resource, row)),
                number: page, size,
                totalElements, totalPages,
                first: page === 0, last: page >= totalPages - 1
            },
            success: true
        };
    }

    // ── Pharmacy list with inventory join ──
    async function getPharmacyList(params) {
        const page = parseInt(params.page) || 0;
        const size = parseInt(params.size) || 20;
        const search = params.search || params.q || null;

        const result = await SupabaseClient.select('medicines', {
            columns: '*, pharmacy_inventory(quantity, selling_price, reorder_level, batch_number, expiry_date)',
            order: 'name.asc',
            limit: size,
            offset: page * size,
            search: search,
            searchColumns: ['name', 'generic_name', 'brand', 'category'],
            count: true
        });

        const totalElements = result.totalCount || result.data.length;
        const totalPages = Math.ceil(totalElements / size) || 1;

        return {
            data: {
                content: result.data.map(row => {
                    // Aggregate inventory batches for this medicine
                    const inv = row.pharmacy_inventory || [];
                    const totalQty = inv.reduce((sum, b) => sum + (b.quantity || 0), 0);
                    const latestPrice = inv.length > 0 ? inv[0].selling_price : null;
                    const reorderLevel = inv.length > 0 ? (inv[0].reorder_level || 10) : 10;

                    return {
                        id: row.id, tenantId: row.tenant_id,
                        name: row.name, genericName: row.generic_name, brand: row.brand,
                        category: row.category, dosageForm: row.dosage_form,
                        strength: row.strength, unit: row.unit, manufacturer: row.manufacturer,
                        description: row.description, requiresPrescription: row.requires_prescription,
                        price: latestPrice,
                        stockQuantity: totalQty,
                        reorderLevel: reorderLevel,
                        createdAt: row.created_at, updatedAt: row.updated_at
                    };
                }),
                number: page, size,
                totalElements, totalPages,
                first: page === 0, last: page >= totalPages - 1
            },
            success: true
        };
    }

    // ── Standard post ──
    async function postToTable(table, body, resource) {
        const data = mapToSupabase(resource, body);
        data.tenant_id = TENANT_ID;

        // Look up patient by MRN if provided
        if (body.patientMrn && !data.patient_id) {
            const pRes = await SupabaseClient.select('patients', {
                columns: 'id', filters: { mrn: `eq.${body.patientMrn}` }
            });
            if (!pRes.data || pRes.data.length === 0) throw new Error('Patient not found with MRN: ' + body.patientMrn);
            data.patient_id = pRes.data[0].id;
        }

        // Lab orders: store testName in notes field since lab_orders has no test_name column
        if (table === 'lab_orders') {
            if (body.testName && !body.notes) data.notes = body.testName;
            else if (body.testName && body.notes) data.notes = `${body.testName} | ${body.notes}`;
            data.order_date = data.order_date || new Date().toISOString();
            delete data.test_name;
            delete data.testName;
        }

        // Auto-generate identifiers
        if (table === 'patients' && !data.mrn) data.mrn = 'MRN-' + Date.now().toString(36).toUpperCase();
        if (table === 'invoices' && !data.invoice_number) {
            data.invoice_number = 'INV-' + Date.now().toString(36).toUpperCase();
            data.invoice_date = data.invoice_date || new Date().toISOString().split('T')[0];
        }
        if (table === 'lab_orders' && !data.order_number) data.order_number = 'LAB-' + Date.now().toString(36).toUpperCase();
        if (table === 'medical_records') {
            data.record_date = data.record_date || new Date().toISOString().split('T')[0];
        }
        if (table === 'appointments') {
            data.appointment_date = data.appointment_date || new Date().toISOString().split('T')[0];
            data.start_time = data.start_time || '09:00';
            if (!data.end_time && data.start_time) {
                const [h, m] = data.start_time.split(':').map(Number);
                const endMin = m + 30;
                data.end_time = `${String(h + Math.floor(endMin / 60)).padStart(2,'0')}:${String(endMin % 60).padStart(2,'0')}`;
            }
            data.status = (data.status || 'scheduled').toLowerCase();
            // Clean: remove fields that don't belong in appointments table
            delete data.admission_reason;
            delete data.expected_discharge_date;
            delete data.actual_discharge_date;
            delete data.admission_number;
            delete data.admission_date;
        }
        if (table === 'invoices') {
            data.status = (data.status || 'draft').toLowerCase();
        }

        if (table === 'admissions') {
            data.admission_number = data.admission_number || 'ADM-' + Math.random().toString(36).substr(2, 9).toUpperCase();
            data.admission_date = data.admission_date || new Date().toISOString();
            data.admission_reason = data.admission_reason || 'Inpatient Admission';
            data.status = 'admitted';
        }

        // Clean: remove fields that don't belong
        if (table === 'invoices') {
            delete data.admission_reason;
            delete data.expected_discharge_date;
        }
        if (table === 'medicines') {
            delete data.admission_reason;
            delete data.price;
            delete data.stock_quantity;
            delete data.reorder_level;
        }

        const result = await SupabaseClient.insert(table, data);
        return { data: mapFromSupabase(resource, result), success: true };
    }

    // ── Login ──
    async function handleLogin(body) {
        const { email, password } = body;
        const result = await SupabaseClient.select('users', {
            columns: '*',
            filters: { email: `eq.${email}` }
        });
        if (!result.data || result.data.length === 0) throw new Error('Invalid email or password');
        const user = result.data[0];
        const authData = {
            accessToken: 'supabase-direct-' + user.id,
            refreshToken: 'supabase-direct-refresh',
            userId: user.id, email: user.email,
            firstName: user.first_name, lastName: user.last_name,
            tenantId: user.tenant_id, tenantName: 'City General Hospital',
            roles: ['ADMIN'], permissions: []
        };
        try {
            const tenant = await SupabaseClient.selectOne('tenants', user.tenant_id);
            if (tenant) authData.tenantName = tenant.name;
        } catch (e) {}
        try {
            const userRoles = await SupabaseClient.select('user_roles', {
                columns: 'role_id,roles(name)',
                filters: { user_id: `eq.${user.id}` }
            });
            if (userRoles.data) authData.roles = userRoles.data.map(ur => ur.roles?.name || 'User');
        } catch (e) {}
        saveAuth(authData);
        return { data: authData, success: true };
    }

    const GLOBAL_TENANT_ID = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';

    async function signup(data) {
        const { firstName, lastName, email, password } = data;
        const tenantId = GLOBAL_TENANT_ID;
        
        // 1. Create User linked to Global Tenant
        const user = await SupabaseClient.insert('users', {
            tenant_id: tenantId,
            email,
            password_hash: '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', // Hardcoded hash for 'password'
            first_name: firstName,
            last_name: lastName,
            is_active: true
        });

        // 2. Create Admin Role and link user
        const adminRole = await SupabaseClient.insert('roles', {
            tenant_id: tenantId,
            name: 'Admin',
            slug: 'admin',
            description: 'Full system access'
        });

        await SupabaseClient.insert('user_roles', {
            user_id: user.id,
            role_id: adminRole.id
        });

        // 3. Get first department
        const depts = await SupabaseClient.select('departments', { filters: { tenant_id: `eq.${tenantId}` }, limit: 1 });
        const deptId = depts.data?.[0]?.id;

        // 4. Create doctor record
        await SupabaseClient.insert('doctors', {
            tenant_id: tenantId,
            user_id: user.id,
            department_id: deptId,
            first_name: firstName,
            last_name: lastName,
            email: email,
            specialization: 'Physician',
            qualification: 'MBBS',
            license_number: 'LP-' + Math.random().toString(36).substr(2, 8).toUpperCase(),
            is_available: true
        });

        return handleLogin({ email, password, tenantSlug: 'city-general-hospital' });
    }

    async function getOne(resource, id) {
        const table = mapTable(resource);
        const data = await SupabaseClient.selectOne(table, id, selectColumns(resource));
        if (!data) throw new Error(`${resource} not found with id ${id}`);
        return { data: mapFromSupabase(resource, data), success: true };
    }

    async function login(email, password, tenantSlug) {
        const json = await handleLogin({ email, password, tenantSlug });
        if (json && json.data) return json.data;
        throw new Error(json?.message || 'Login failed');
    }

    async function logout() { clearAuth(); window.location.href = 'login.html'; }

    // ── Dashboard Stats ──
    async function getDashboardStats() {
        const [patients, doctors, todayAppts, invoices] = await Promise.all([
            SupabaseClient.select('patients', { count: true, limit: 0 }),
            SupabaseClient.select('doctors', { count: true, limit: 0 }),
            SupabaseClient.select('appointments', {
                count: true, limit: 0,
                filters: { appointment_date: `eq.${new Date().toISOString().split('T')[0]}` }
            }),
            SupabaseClient.select('invoices', { columns: 'total_amount', limit: 1000 })
        ]);
        const totalRevenue = invoices.data.reduce((sum, inv) => sum + parseFloat(inv.total_amount || 0), 0);
        return {
            data: {
                totalPatients: patients.totalCount || 0,
                totalDoctors: doctors.totalCount || 0,
                todayAppointments: todayAppts.totalCount || 0,
                totalRevenue
            }, success: true
        };
    }

    // ── Billing Stats ──
    async function getBillingStats() {
        const res = await SupabaseClient.select('invoices', { columns: 'total_amount,paid_amount,status', limit: 5000 });
        let totalRevenue = 0, totalCollected = 0, pendingAmount = 0, overdueCount = 0;
        res.data.forEach(inv => {
            const total = parseFloat(inv.total_amount || 0);
            const paid = parseFloat(inv.paid_amount || 0);
            const st = (inv.status || '').toLowerCase();
            totalRevenue += total;
            totalCollected += paid;
            if (st !== 'paid' && st !== 'cancelled' && st !== 'refunded') {
                pendingAmount += total - paid;
            }
            if (st === 'overdue') overdueCount++;
        });
        return { data: { totalRevenue, totalCollected, pendingAmount, overdueCount }, success: true };
    }

    // ── Patient EMR records ──
    async function getPatientRecords(patientId) {
        const res = await SupabaseClient.select('medical_records', {
            columns: selectColumns('emr'),
            filters: { patient_id: `eq.${patientId}` },
            order: 'created_at.desc'
        });
        return { data: res.data.map(r => mapFromSupabase('emr', r)), success: true };
    }

    // ── Inpatient: Beds with status ──
    async function getBedsWithStatus() {
        const beds = await SupabaseClient.select('beds', {
            columns: '*, wards(name)',
            order: 'bed_number.asc'
        });
        const admissions = await SupabaseClient.select('admissions', {
            columns: '*, patients(first_name, last_name)',
            filters: { status: 'eq.admitted' }
        });
        const occupiedBedIds = {};
        (admissions.data || []).forEach(a => {
            occupiedBedIds[a.bed_id] = a;
        });
        const result = beds.data.map(b => {
            const adm = occupiedBedIds[b.id];
            return {
                id: b.id, bedNumber: b.bed_number, wardName: b.wards?.name || 'General',
                bedType: b.bed_type, dailyRate: b.daily_rate,
                status: adm ? 'OCCUPIED' : (b.status || 'AVAILABLE').toUpperCase(),
                patientName: adm ? `${adm.patients?.first_name || ''} ${adm.patients?.last_name || ''}`.trim() : null,
                admissionId: adm?.id
            };
        });
        return { data: result, success: true };
    }

    // ── Inpatient: Admit patient ──
    async function admitPatient(body) {
        let patientId = body.patientId;
        if (!patientId && body.patientMrn) {
            const pRes = await SupabaseClient.select('patients', {
                columns: 'id', filters: { mrn: `eq.${body.patientMrn}` }
            });
            if (!pRes.data || pRes.data.length === 0) throw new Error('Patient not found with MRN: ' + body.patientMrn);
            patientId = pRes.data[0].id;
        }
        if (!body.doctorId) throw new Error('Doctor is required for admission');
        const admissionData = {
            tenant_id: TENANT_ID,
            patient_id: patientId,
            doctor_id: body.doctorId,
            bed_id: body.bedId,
            admission_number: 'ADM-' + Math.random().toString(36).substr(2, 9).toUpperCase(),
            admission_date: new Date().toISOString(),
            admission_reason: body.admissionReason || body.reason || 'Inpatient Admission',
            diagnosis: body.diagnosis || null,
            expected_discharge_date: body.expectedDischargeDate || null,
            notes: body.notes || null,
            status: 'admitted'
        };
        const admission = await SupabaseClient.insert('admissions', admissionData);
        if (body.bedId) {
            try { await SupabaseClient.update('beds', body.bedId, { status: 'occupied' }); } catch(e) {}
        }
        return { data: admission, success: true };
    }

    // ── Inpatient: Discharge ──
    async function dischargePatient(bedId) {
        const res = await SupabaseClient.select('admissions', {
            columns: 'id',
            filters: { bed_id: `eq.${bedId}`, status: 'eq.admitted' }
        });
        if (res.data && res.data.length > 0) {
            await SupabaseClient.update('admissions', res.data[0].id, {
                status: 'discharged',
                actual_discharge_date: new Date().toISOString(),
                updated_at: new Date().toISOString()
            });
        }
        try { await SupabaseClient.update('beds', bedId, { status: 'available' }); } catch(e) {}
        return { success: true };
    }

    // ── Appointment status update ──
    // Valid: scheduled, confirmed, checked_in, in_progress, completed, cancelled, no_show
    async function updateAppointmentStatus(id, status) {
        const validStatuses = ['scheduled','confirmed','checked_in','in_progress','completed','cancelled','no_show'];
        const s = status.toLowerCase();
        if (!validStatuses.includes(s)) throw new Error(`Invalid status: ${status}. Valid: ${validStatuses.join(', ')}`);
        const result = await SupabaseClient.update('appointments', id, {
            status: s,
            updated_at: new Date().toISOString()
        });
        return { data: result, success: true };
    }

    // ── Table mapping ──
    function mapTable(resource) {
        const map = {
            patients: 'patients', doctors: 'doctors', appointments: 'appointments',
            billing: 'invoices', invoices: 'invoices',
            pharmacy: 'medicines', medicines: 'medicines',
            lab: 'lab_orders', 'lab-orders': 'lab_orders',
            emr: 'medical_records', 'medical-records': 'medical_records',
            inpatient: 'admissions', admissions: 'admissions',
            wards: 'wards', beds: 'beds',
            departments: 'departments', users: 'users'
        };
        const base = resource.split('/')[0];
        return map[resource] || map[base] || resource;
    }

    function selectColumns(resource) {
        const map = {
            // Doctors: name columns are directly on doctors table now
            doctors: 'id,tenant_id,department_id,specialization,qualification,license_number,experience_years,consultation_fee,bio,is_available,first_name,last_name,email,phone,created_at,updated_at,deleted_at,departments(name)',
            // Appointments: join doctors directly for first_name/last_name
            appointments: '*, patients(first_name,last_name,mrn), doctors(first_name,last_name,specialization)',
            // Lab: join patients + doctors directly
            lab: 'id,tenant_id,patient_id,doctor_id,order_number,order_date,priority,status,notes,created_at,updated_at, patients(first_name,last_name,mrn), doctors(first_name,last_name)',
            lab_orders: 'id,tenant_id,patient_id,doctor_id,order_number,order_date,priority,status,notes,created_at,updated_at, patients(first_name,last_name,mrn), doctors(first_name,last_name)',
            // EMR: join patients + doctors directly
            emr: '*, patients(first_name,last_name,mrn), doctors(first_name,last_name)',
            medical_records: '*, patients(first_name,last_name,mrn), doctors(first_name,last_name)',
            // Admissions: join patients, doctors, beds
            admissions: '*, patients(first_name,last_name,mrn), doctors(first_name,last_name), beds(bed_number, wards(name))',
            inpatient: '*, patients(first_name,last_name,mrn), doctors(first_name,last_name), beds(bed_number, wards(name))',
            invoices: '*, patients(first_name,last_name,mrn)',
            billing: '*, patients(first_name,last_name,mrn)',
            beds: '*, wards(name)'
        };
        return map[resource] || '*';
    }

    function searchColumnsFor(resource) {
        const map = {
            patients: ['first_name', 'last_name', 'mrn', 'phone', 'email'],
            doctors: ['specialization', 'license_number', 'first_name', 'last_name'],
            pharmacy: ['name', 'generic_name', 'brand', 'category'],
            medicines: ['name', 'generic_name', 'brand', 'category'],
            billing: ['invoice_number'], invoices: ['invoice_number'],
            lab: ['order_number'], lab_orders: ['order_number'],
            wards: ['name'], beds: ['bed_number']
        };
        return map[resource] || [];
    }

    function mapColumn(col) {
        const map = {
            firstName: 'first_name', lastName: 'last_name',
            dateOfBirth: 'date_of_birth', bloodGroup: 'blood_group',
            createdAt: 'created_at', updatedAt: 'updated_at',
            appointmentDate: 'appointment_date', startTime: 'start_time',
            endTime: 'end_time', invoiceNumber: 'invoice_number',
            invoiceDate: 'invoice_date', totalAmount: 'total_amount',
            orderDate: 'order_date', orderNumber: 'order_number',
            admissionDate: 'admission_date', isActive: 'is_active',
            patientId: 'patient_id', doctorId: 'doctor_id'
        };
        return map[col] || col;
    }

    // ── Data mapping: Supabase → camelCase ──
    function mapFromSupabase(resource, row) {
        if (!row) return row;
        const base = {
            id: row.id, tenantId: row.tenant_id,
            createdAt: row.created_at, updatedAt: row.updated_at, isActive: row.is_active
        };
        switch (resource) {
            case 'patients':
                return { ...base,
                    mrn: row.mrn, firstName: row.first_name, lastName: row.last_name,
                    dateOfBirth: row.date_of_birth, gender: row.gender, bloodGroup: row.blood_group,
                    phone: row.phone, email: row.email,
                    emergencyContactName: row.emergency_contact_name, emergencyContactPhone: row.emergency_contact_phone,
                    address: row.address, city: row.city, state: row.state,
                    zipCode: row.zip_code, country: row.country,
                    insuranceProvider: row.insurance_provider, insurancePolicyNumber: row.insurance_policy_number,
                    allergies: row.allergies, chronicConditions: row.chronic_conditions, tags: row.tags, notes: row.notes
                };
            case 'doctors':
                return { ...base,
                    departmentId: row.department_id,
                    specialization: row.specialization, qualification: row.qualification,
                    licenseNumber: row.license_number, experienceYears: row.experience_years,
                    consultationFee: row.consultation_fee, bio: row.bio, isAvailable: row.is_available,
                    firstName: row.first_name || '',
                    lastName: row.last_name || '',
                    email: row.email || '',
                    phone: row.phone || '',
                    departmentName: row.departments?.name,
                    doctorName: `Dr. ${row.first_name || ''} ${row.last_name || ''}`.trim()
                };
            case 'appointments':
                return { ...base,
                    patientId: row.patient_id, doctorId: row.doctor_id,
                    appointmentDate: row.appointment_date, startTime: row.start_time,
                    endTime: row.end_time, status: row.status, type: row.type,
                    reason: row.reason, notes: row.notes, tokenNumber: row.token_number,
                    appointmentTime: row.appointment_date ? `${row.appointment_date}T${row.start_time || '09:00'}` : null,
                    patientName: row.patients ? `${row.patients.first_name} ${row.patients.last_name}` : '',
                    doctorName: row.doctors ? `Dr. ${row.doctors.first_name || ''} ${row.doctors.last_name || ''}`.trim() : ''
                };
            case 'billing': case 'invoices':
                return { ...base,
                    patientId: row.patient_id, invoiceNumber: row.invoice_number,
                    invoiceDate: row.invoice_date, dueDate: row.due_date,
                    subtotal: row.subtotal, taxAmount: row.tax_amount,
                    discountAmount: row.discount_amount, totalAmount: row.total_amount,
                    paidAmount: row.paid_amount, status: row.status, notes: row.notes,
                    patientName: row.patients ? `${row.patients.first_name} ${row.patients.last_name}` : ''
                };
            case 'pharmacy': case 'medicines':
                return { ...base,
                    name: row.name, genericName: row.generic_name, brand: row.brand,
                    category: row.category, dosageForm: row.dosage_form,
                    strength: row.strength, unit: row.unit, manufacturer: row.manufacturer,
                    description: row.description, requiresPrescription: row.requires_prescription,
                    price: 0, stockQuantity: 0, reorderLevel: 10
                };
            case 'lab': case 'lab_orders':
                return { ...base,
                    patientId: row.patient_id, doctorId: row.doctor_id,
                    orderNumber: row.order_number, orderDate: row.order_date,
                    priority: row.priority, status: row.status, notes: row.notes,
                    testName: row.notes || 'Lab Test',
                    patientName: row.patients ? `${row.patients.first_name} ${row.patients.last_name}` : '',
                    doctorName: row.doctors ? `Dr. ${row.doctors.first_name || ''} ${row.doctors.last_name || ''}`.trim() : ''
                };
            case 'emr': case 'medical_records':
                return { ...base,
                    patientId: row.patient_id, doctorId: row.doctor_id,
                    appointmentId: row.appointment_id, recordDate: row.record_date,
                    chiefComplaint: row.chief_complaint, diagnosis: row.diagnosis,
                    treatmentPlan: row.treatment_plan, assessment: row.diagnosis,
                    plan: row.treatment_plan,
                    vitals: row.vitals, followUpDate: row.follow_up_date,
                    status: row.status, version: row.version,
                    recordedAt: row.record_date || row.created_at,
                    patientName: row.patients ? `${row.patients.first_name} ${row.patients.last_name}` : '',
                    doctorName: row.doctors ? `Dr. ${row.doctors.first_name || ''} ${row.doctors.last_name || ''}`.trim() : ''
                };
            case 'inpatient': case 'admissions':
                return { ...base,
                    patientId: row.patient_id, doctorId: row.doctor_id,
                    bedId: row.bed_id, admissionNumber: row.admission_number,
                    admissionDate: row.admission_date,
                    expectedDischargeDate: row.expected_discharge_date,
                    actualDischargeDate: row.actual_discharge_date,
                    admissionReason: row.admission_reason, diagnosis: row.diagnosis,
                    status: row.status, notes: row.notes,
                    patientName: row.patients ? `${row.patients.first_name} ${row.patients.last_name}` : '',
                    doctorName: row.doctors ? `Dr. ${row.doctors.first_name || ''} ${row.doctors.last_name || ''}`.trim() : '',
                    bedNumber: row.beds?.bed_number, wardName: row.beds?.wards?.name
                };
            default: return row;
        }
    }

    // ── Data mapping: camelCase → snake_case ──
    function mapToSupabase(resource, body) {
        if (!body) return body;
        const result = { tenant_id: TENANT_ID };
        const mapping = {
            firstName: 'first_name', lastName: 'last_name',
            dateOfBirth: 'date_of_birth', bloodGroup: 'blood_group',
            emergencyContactName: 'emergency_contact_name', emergencyContactPhone: 'emergency_contact_phone',
            zipCode: 'zip_code', insuranceProvider: 'insurance_provider',
            insurancePolicyNumber: 'insurance_policy_number', chronicConditions: 'chronic_conditions',
            isActive: 'is_active', patientId: 'patient_id', doctorId: 'doctor_id',
            testName: '_testName',
            appointmentDate: 'appointment_date', startTime: 'start_time', endTime: 'end_time',
            appointmentTime: '_appointmentTime',
            tokenNumber: 'token_number', invoiceNumber: 'invoice_number', invoiceDate: 'invoice_date',
            dueDate: 'due_date', taxAmount: 'tax_amount', discountAmount: 'discount_amount',
            totalAmount: 'total_amount', paidAmount: 'paid_amount',
            genericName: 'generic_name', dosageForm: 'dosage_form',
            requiresPrescription: 'requires_prescription',
            orderNumber: 'order_number', orderDate: 'order_date',
            diagnosis: 'diagnosis',
            chiefComplaint: 'chief_complaint', treatmentPlan: 'treatment_plan',
            followUpDate: 'follow_up_date', recordDate: 'record_date',
            admissionDate: 'admission_date', admissionNumber: 'admission_number',
            admissionReason: 'admission_reason', reason: 'admission_reason',
            expectedDischargeDate: 'expected_discharge_date', actualDischargeDate: 'actual_discharge_date',
            bedId: 'bed_id', wardId: 'ward_id', bedNumber: 'bed_number', bedType: 'bed_type',
            dailyRate: 'daily_rate', totalBeds: 'total_beds',
            tenantId: 'tenant_id', userId: 'user_id',
            departmentId: 'department_id', licenseNumber: 'license_number',
            experienceYears: 'experience_years', consultationFee: 'consultation_fee',
            isAvailable: 'is_available', appointmentId: 'appointment_id',
            patientMrn: '_patientMrn'
        };

        for (const [key, val] of Object.entries(body)) {
            if (key === 'appointmentTime' && val) {
                const dt = new Date(val);
                result.appointment_date = dt.toISOString().split('T')[0];
                result.start_time = dt.toTimeString().slice(0, 5);
                continue;
            }
            if (key === 'assessment') { result.diagnosis = val; continue; }
            if (key === 'plan') { result.treatment_plan = val; continue; }
            if (key === 'recordedAt') { result.record_date = val; continue; }
            if (key === 'patientMrn') continue;
            // Handle array fields
            if (['allergies', 'chronicConditions', 'tags'].includes(key) && typeof val === 'string') {
                const snakeKey = mapping[key] || key;
                result[snakeKey] = val.split(',').map(s => s.trim()).filter(Boolean);
                continue;
            }
            // Handle vitals as JSON
            if (key === 'vitals' && typeof val === 'object') {
                result.vitals = val;
                continue;
            }
            // Cast numeric fields
            if (key === 'experienceYears' && val !== '' && val != null) {
                result.experience_years = parseInt(val) || 0;
                continue;
            }
            if (key === 'consultationFee' && val !== '' && val != null) {
                result.consultation_fee = parseFloat(val) || 0;
                continue;
            }
            // Cast billing numeric fields
            if (['totalAmount','paidAmount','subtotal','taxAmount','discountAmount','price','dailyRate'].includes(key) && val !== '' && val != null) {
                const snk = mapping[key] || key;
                result[snk] = parseFloat(val) || 0;
                continue;
            }
            const snakeKey = mapping[key] || key;
            if (snakeKey.startsWith('_')) continue;
            result[snakeKey] = val;
        }
        return result;
    }

    // ── Helpers ──
    function formatCurrency(n) {
        if (n == null) return '₹0';
        const num = Number(n);
        if (num >= 100000) return '₹' + (num / 100000).toFixed(2) + 'L';
        if (num >= 1000) return '₹' + num.toLocaleString('en-IN');
        return '₹' + num.toFixed(2);
    }

    function formatDate(d) {
        if (!d) return '—';
        return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    function initials(firstName, lastName) {
        return ((firstName?.[0] || '') + (lastName?.[0] || '')).toUpperCase();
    }

    function initSidebar() {
        const page = window.location.pathname.split('/').pop();
        document.querySelectorAll('.sidebar-item').forEach(el => {
            el.classList.toggle('active', el.getAttribute('href') === page);
        });
        const logoutBtn = document.querySelector('[data-action="logout"], a[href="login.html"]');
        if (logoutBtn && isLoggedIn()) {
            logoutBtn.addEventListener('click', e => { e.preventDefault(); logout(); });
        }
        const user = getUser();
        if (user) {
            const nameEl = document.querySelector('.sidebar-user-name, aside .font-semibold');
            const roleEl = document.querySelector('.sidebar-user-role, aside .text-outline');
            if (nameEl) nameEl.textContent = `${user.firstName} ${user.lastName}`;
            if (roleEl) roleEl.textContent = user.roles ? [...user.roles].join(', ') : '';
        }
    }

    // ── Dropdown Helpers ──
    async function loadPatients() {
        const res = await SupabaseClient.select('patients', {
            columns: 'id,first_name,last_name,mrn',
            order: 'first_name.asc', limit: 500
        });
        return (res.data || []).map(p => ({
            id: p.id, mrn: p.mrn,
            name: `${p.first_name} ${p.last_name} (${p.mrn})`
        }));
    }

    async function loadDoctors() {
        const res = await SupabaseClient.select('doctors', {
            columns: 'id,first_name,last_name,specialization',
            order: 'created_at.asc', limit: 200
        });
        return (res.data || []).map(d => ({
            id: d.id,
            name: `Dr. ${d.first_name || ''} ${d.last_name || ''} (${d.specialization || ''})`.trim()
        }));
    }

    async function loadDepartments() {
        const res = await SupabaseClient.select('departments', {
            columns: 'id,name,code',
            order: 'name.asc', limit: 100
        });
        return (res.data || []).map(d => ({
            id: d.id, name: d.name, code: d.code
        }));
    }

    async function loadBeds() {
        const res = await SupabaseClient.select('beds', {
            columns: 'id,bed_number,bed_type,status,ward_id,wards(name)',
            order: 'bed_number.asc', limit: 200
        });
        return (res.data || []).map(b => ({
            id: b.id, bedNumber: b.bed_number, bedType: b.bed_type,
            status: b.status, wardName: b.wards?.name || 'General'
        }));
    }

    return {
        get, getOne, post, put, patch, del, login, logout, signup,
        getToken, getUser, isLoggedIn, requireAuth, clearAuth,
        formatCurrency, formatDate, initials, initSidebar,
        loadPatients, loadDoctors, loadDepartments, loadBeds,
        updateAppointmentStatus,
        BASE_URL: SupabaseClient.SUPABASE_URL
    };
})();
