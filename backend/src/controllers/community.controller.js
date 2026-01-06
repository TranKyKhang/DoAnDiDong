const db=require("../config/db");

exports.getAllCommunity=async (req,res)=>{
    try{
        const [rows]= await db.query("SELECT * FROM COMMUNITIES");
        res.json({
            success: true,
            data: rows
        })
    }catch(err){
        console.error("MYSQL ERROR FULL:", err); 
        res.status(500).json({
        success: false,
        message: err.message
    });
    }
}