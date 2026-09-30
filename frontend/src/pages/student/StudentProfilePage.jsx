import React, { useState, useEffect } from 'react';
import { studentService } from '../../services/studentService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/Badge';
import { FiUser, FiMail, FiPhone, FiBook, FiLayers, FiCalendar, FiShield } from 'react-icons/fi';

export const StudentProfilePage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProfile = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await studentService.getMyProfile();
      // Handle either res.data or res
      const profileData = res?.data || res;
      setProfile(profileData);
    } catch (err) {
      setError(err.friendlyMessage || err.response?.data?.message || err.message || 'Failed to load profile.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  if (loading) return <LoadingSpinner message="Loading academic profile..." />;
  if (error) return <ErrorState message={error} onRetry={fetchProfile} />;

  const formatProgram = (code, name) => {
    const c = (code || '').toUpperCase();
    if (c.includes('AIML')) return 'CSE – AIML';
    if (c.includes('IOT')) return 'CSE – IoT';
    if (c.includes('RAI')) return 'R&AI';
    return name || code || '-';
  };

  const formatSemester = (sem) => {
    if (!sem) return '-';
    const roman = { 1: 'I', 2: 'II', 3: 'III', 4: 'IV', 5: 'V', 6: 'VI', 7: 'VII', 8: 'VIII' };
    return roman[sem] ? `Semester ${roman[sem]}` : `Semester ${sem}`;
  };

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      <PageHeader
        title="Student Profile"
        subtitle="Official academic record and personal identification"
      />

      <div className="bg-theme-surface rounded-2xl border border-theme shadow-md overflow-hidden">
        {/* Profile Header banner using theme primary */}
        <div
          className="px-6 py-8 text-white relative overflow-hidden"
          style={{ backgroundColor: 'var(--color-primary)' }}
        >
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-5 relative z-10">
            <div className="w-20 h-20 rounded-2xl bg-white/15 backdrop-blur-md flex items-center justify-center text-3xl font-extrabold border border-white/20 shadow-md">
              {profile?.firstName?.charAt(0) || <FiUser />}
            </div>
            <div className="text-center sm:text-left">
              <h2 className="text-2xl font-extrabold tracking-tight">
                {profile?.firstName} {profile?.lastName}
              </h2>
              <p className="text-white/90 text-sm mt-0.5 font-mono font-semibold">
                Roll Number: {profile?.rollNumber}
              </p>
              <p className="text-white/80 text-xs mt-0.5 font-medium">
                {formatProgram(profile?.programCode, profile?.programName)}
              </p>
              <div className="mt-3 flex flex-wrap gap-2 justify-center sm:justify-start">
                <span className="px-3 py-1 rounded-full text-xs font-semibold bg-white/20 text-white border border-white/20">
                  {formatSemester(profile?.currentSemester)}
                </span>
                <span className="px-3 py-1 rounded-full text-xs font-semibold bg-white/20 text-white border border-white/20">
                  Section {profile?.section || 'A'}
                </span>
                <StatusBadge status={profile?.active !== false ? 'ACTIVE' : 'INACTIVE'} />
              </div>
            </div>
          </div>
        </div>

        {/* Academic and Contact Info */}
        <div className="p-6 sm:p-8 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-theme-muted flex items-center gap-1.5">
              <FiBook className="w-3.5 h-3.5" /> Academic Information
            </h3>
            <div className="bg-theme-elevated p-5 rounded-xl border border-theme space-y-3.5 text-sm">
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Roll Number</span>
                <span className="font-mono font-bold text-theme-brand">{profile?.rollNumber || '-'}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Program</span>
                <span className="font-bold text-theme-primary text-right max-w-[220px]">
                  {formatProgram(profile?.programCode, profile?.programName)}
                </span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Program Code</span>
                <span className="font-mono font-semibold text-theme-brand">{profile?.programCode || '-'}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Section</span>
                <span className="font-bold text-theme-brand">Section {profile?.section || 'A'}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Department</span>
                <span className="font-semibold text-theme-primary">Department of CSE (AIML & IoT), R&AI</span>
              </div>
              <div className="flex justify-between items-center py-1">
                <span className="text-theme-secondary">Admission Year / Batch</span>
                <span className="font-semibold text-theme-primary">{profile?.admissionYear || '-'}</span>
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-theme-muted flex items-center gap-1.5">
              <FiShield className="w-3.5 h-3.5" /> Contact & Verification
            </h3>
            <div className="bg-theme-elevated p-5 rounded-xl border border-theme space-y-3.5 text-sm">
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary flex items-center gap-1.5">
                  <FiMail className="w-3.5 h-3.5" /> Institutional Email
                </span>
                <span className="font-semibold text-theme-primary truncate max-w-[190px]">{profile?.email || '-'}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary flex items-center gap-1.5">
                  <FiPhone className="w-3.5 h-3.5" /> Phone Number
                </span>
                <span className="font-semibold text-theme-primary">{profile?.phone || profile?.phoneNumber || '-'}</span>
              </div>
              <div className="flex justify-between items-center py-1 border-b border-theme-subtle">
                <span className="text-theme-secondary">Current Semester</span>
                <span className="font-bold text-theme-primary">{formatSemester(profile?.currentSemester)}</span>
              </div>
              <div className="flex justify-between items-center py-1">
                <span className="text-theme-secondary">Verification Status</span>
                <span className="font-bold text-emerald-600 dark:text-emerald-400">Verified Student</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentProfilePage;
