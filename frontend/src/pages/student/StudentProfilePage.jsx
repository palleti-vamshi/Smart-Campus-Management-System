import React, { useState, useEffect } from 'react';
import { studentService } from '../../services/studentService';
import PageHeader from '../../components/common/PageHeader';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorState from '../../components/common/ErrorState';
import StatusBadge from '../../components/common/Badge';
import { FiUser, FiMail, FiPhone, FiBook, FiLayers, FiCalendar } from 'react-icons/fi';

export const StudentProfilePage = () => {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchProfile = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await studentService.getMyProfile();
      setProfile(res.data);
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to load profile.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  if (loading) return <LoadingSpinner message="Loading profile..." />;
  if (error) return <ErrorState message={error} onRetry={fetchProfile} />;

  return (
    <div className="space-y-6 max-w-4xl">
      <PageHeader
        title="Student Profile"
        subtitle="Official academic record and personal identification"
      />

      <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
        {/* Profile Header banner */}
        <div className="bg-gradient-to-r from-primary-800 to-primary-600 px-6 py-8 text-white">
          <div className="flex flex-col sm:flex-row items-center sm:items-start gap-5">
            <div className="w-20 h-20 rounded-2xl bg-white/10 backdrop-blur-md flex items-center justify-center text-3xl font-bold border border-white/20">
              {profile?.firstName?.charAt(0) || <FiUser />}
            </div>
            <div className="text-center sm:text-left">
              <h2 className="text-2xl font-bold">{profile?.firstName} {profile?.lastName}</h2>
              <p className="text-primary-100 text-sm mt-0.5 font-medium">{profile?.rollNumber}</p>
              <div className="mt-2 flex flex-wrap gap-2 justify-center sm:justify-start">
                <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-white/20 text-white">
                  Semester {profile?.currentSemester}
                </span>
                <span className="px-2.5 py-0.5 rounded-full text-xs font-medium bg-white/20 text-white">
                  Section {profile?.section}
                </span>
                <StatusBadge status={profile?.active ? 'ACTIVE' : 'INACTIVE'} />
              </div>
            </div>
          </div>
        </div>

        {/* Academic and Contact Info */}
        <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Academic Details</h3>
            <div className="bg-slate-50 p-4 rounded-xl space-y-3 text-sm">
              <div className="flex justify-between">
                <span className="text-slate-500">Program</span>
                <span className="font-semibold text-slate-800">{profile?.programName || profile?.programCode}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Program Code</span>
                <span className="font-semibold text-slate-800">{profile?.programCode}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Department</span>
                <span className="font-semibold text-slate-800">{profile?.departmentName || 'Engineering'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Admission Year</span>
                <span className="font-semibold text-slate-800">{profile?.admissionYear}</span>
              </div>
            </div>
          </div>

          <div className="space-y-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Contact Information</h3>
            <div className="bg-slate-50 p-4 rounded-xl space-y-3 text-sm">
              <div className="flex justify-between items-center">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <FiMail className="w-3.5 h-3.5" /> Email
                </span>
                <span className="font-semibold text-slate-800">{profile?.email}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-slate-500 flex items-center gap-1.5">
                  <FiPhone className="w-3.5 h-3.5" /> Phone
                </span>
                <span className="font-semibold text-slate-800">{profile?.phoneNumber || 'Not provided'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default StudentProfilePage;
